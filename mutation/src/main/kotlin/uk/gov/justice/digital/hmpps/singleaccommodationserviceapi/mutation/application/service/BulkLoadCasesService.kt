package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.BulkLoadCasesErrorDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.BulkLoadCasesResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpstreamFailureDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailureTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.TeamCodesRequiredException

@Service
class BulkLoadCasesService(
  private val teamCaseOrchestrationService: TeamCaseOrchestrationService,
  private val caseApplicationService: CaseApplicationService,
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestService: CaseRefreshRequestService?,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  fun bulkLoadCases(teamCodes: List<String>, dryRun: Boolean): ApiResponseDto<BulkLoadCasesResultDto> {
    caseRefreshRequestService ?: throw IllegalStateException("Case refresh request service is not enabled")

    val normalizedTeamCodes = teamCodes.map { it.trim().uppercase() }.filter { it.isNotEmpty() }.distinct()
    if (normalizedTeamCodes.isEmpty()) throw TeamCodesRequiredException()

    log.info("Bulk loading {} team(s), dryRun={}", normalizedTeamCodes.size, dryRun)

    val errors = mutableListOf<BulkLoadCasesErrorDto>()
    val results = normalizedTeamCodes.mapNotNull { teamCode ->
      try {
        loadTeam(teamCode, dryRun)
      } catch (exception: Exception) {
        log.error("Bulk load failed for team {}", teamCode, exception)
        errors += BulkLoadCasesErrorDto(teamCode, exception.message ?: "Unexpected error")
        null
      }
    }

    return ApiResponseDto(
      data = BulkLoadCasesResultDto(
        dryRun = dryRun,
        teamsProcessed = results.count { it.upstreamFailures.isEmpty() },
        crnsFound = results.sumOf { it.crnsFound },
        casesAlreadyPresent = results.sumOf { it.casesAlreadyPresent },
        casesCreated = results.sumOf { it.casesCreated },
        refreshesRequested = results.sumOf { it.refreshesRequested },
        errors = errors,
      ),
      upstreamFailures = results.flatMap { it.upstreamFailures },
    ).also { log.info("Bulk load finished: {}", it.data) }
  }

  private fun loadTeam(teamCode: String, dryRun: Boolean): TeamLoadResult {
    val teamCasesResult = teamCaseOrchestrationService.getCasesByTeamCode(teamCode)

    if (teamCasesResult.upstreamFailures.isNotEmpty()) {
      log.error("Could not retrieve cases for team {}, team has been skipped", teamCode)
      return TeamLoadResult(
        upstreamFailures = teamCasesResult.upstreamFailures.map(UpstreamFailureTransformer::toUpstreamFailureDto),
      )
    }

    val teamCases = teamCasesResult.data

    if (teamCases.isEmpty()) {
      log.info("Team {} has no cases", teamCode)
      return TeamLoadResult()
    }

    val unpersistedCrns = caseRepository.findUnpersistedCrns(teamCases.map { it.crn }.toTypedArray())
    val casesAlreadyPresent = teamCases.size - unpersistedCrns.size

    if (dryRun) {
      log.info(
        "[dry run] Team {}: {} case(s), {} already present, {} would be created",
        teamCode,
        teamCases.size,
        casesAlreadyPresent,
        unpersistedCrns.size,
      )
      return TeamLoadResult(crnsFound = teamCases.size, casesAlreadyPresent = casesAlreadyPresent)
    }

    caseApplicationService.createCases(teamCases.map { CrnToPrisonNumber(it.crn, it.prisonNumber) })

    val caseIds = caseRepository.findByCrns(teamCases.map { it.crn }).map { it.id }
    caseRefreshRequestService?.requestBulkRefresh(caseIds)
    log.info("Team {}: requested refresh for {} of {} case(s)", teamCode, caseIds.size, teamCases.size)

    return TeamLoadResult(
      crnsFound = teamCases.size,
      casesAlreadyPresent = casesAlreadyPresent,
      casesCreated = unpersistedCrns.size,
      refreshesRequested = caseIds.size,
    )
  }

  private data class TeamLoadResult(
    val crnsFound: Int = 0,
    val casesAlreadyPresent: Int = 0,
    val casesCreated: Int = 0,
    val refreshesRequested: Int = 0,
    val upstreamFailures: List<UpstreamFailureDto> = emptyList(),
  )
}
