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
import java.time.Duration

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

    val normalizedTeamCodes = teamCodes.map { it.trim().uppercase() }
      .filter(String::isNotEmpty)
      .distinct()
      .ifEmpty { throw TeamCodesRequiredException() }

    log.info("Bulk loading {} team(s), dryRun={}", normalizedTeamCodes.size, dryRun)
    val startedAt = System.nanoTime()

    val errors = mutableListOf<BulkLoadCasesErrorDto>()
    val results = normalizedTeamCodes.mapNotNull { teamCode ->
      val teamStartedAt = System.nanoTime()
      try {
        loadTeam(teamCode, dryRun).also {
          log.info("Team {} finished in {}ms", teamCode, millisSince(teamStartedAt))
        }
      } catch (exception: Exception) {
        log.error("Bulk load failed for team {} after {}ms", teamCode, millisSince(teamStartedAt), exception)
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
    ).also {
      log.info(
        "Bulk load finished in {}ms: {}. Enqueued refreshes are processed asynchronously by CaseRefreshWorker",
        millisSince(startedAt),
        it.data,
      )
    }
  }

  private fun loadTeam(teamCode: String, dryRun: Boolean): TeamLoadResult {
    val fetchStartedAt = System.nanoTime()
    val teamCasesResult = teamCaseOrchestrationService.getCasesByTeamCode(teamCode)
    log.info(
      "Team {}: retrieved {} case(s) from upstream in {}ms, {} upstream failure(s)",
      teamCode,
      teamCasesResult.data.size,
      millisSince(fetchStartedAt),
      teamCasesResult.upstreamFailures.size,
    )

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

    val writeStartedAt = System.nanoTime()
    caseApplicationService.createCases(teamCases.map { CrnToPrisonNumber(it.crn, it.prisonerNumber) })

    val caseIds = caseRepository.findByCrns(teamCases.map { it.crn }).map { it.id }
    caseRefreshRequestService?.requestBulkRefresh(caseIds)
    log.info(
      "Team {}: requested refresh for {} of {} case(s) ({} created) in {}ms",
      teamCode,
      caseIds.size,
      teamCases.size,
      unpersistedCrns.size,
      millisSince(writeStartedAt),
    )

    return TeamLoadResult(
      crnsFound = teamCases.size,
      casesAlreadyPresent = casesAlreadyPresent,
      casesCreated = unpersistedCrns.size,
      refreshesRequested = caseIds.size,
    )
  }

  private fun millisSince(startNanos: Long) = Duration.ofNanos(System.nanoTime() - startNanos).toMillis()

  private data class TeamLoadResult(
    val crnsFound: Int = 0,
    val casesAlreadyPresent: Int = 0,
    val casesCreated: Int = 0,
    val refreshesRequested: Int = 0,
    val upstreamFailures: List<UpstreamFailureDto> = emptyList(),
  )
}
