package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PeopleType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.UpstreamFailureException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailureTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.toAssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toLimitedCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import java.time.Clock
import java.time.LocalDate

@Service
class CaseQueryService(
  private val caseOrchestrationService: CaseOrchestrationService,
  private val limitedAccessService: LimitedAccessService,
  private val userService: UserService,
  private val caseRepository: CaseRepository,
  private val clock: Clock,
) {

  fun getCaseList(teamCode: String?): ApiResponseDto<Map<Case, UserAccess?>> {
    val user = userService.authorizeAndRetrieveUser()
    val caseOrchestrationResult = caseOrchestrationService.getCaseList(user.username, teamCode)
    limitedAccessService.throwIfUserAccessFailed(caseOrchestrationResult.upstreamFailures)

    val crnsToUserAccess = limitedAccessService.getCrnToUserAccess(caseOrchestrationResult.data.caseAccess)
    val cases = caseOrchestrationResult.data.cases.associateWith { case -> crnsToUserAccess[case.crn] }

    return toApiResponseDto(
      data = cases,
      upstreamFailures = caseOrchestrationResult.upstreamFailures,
    )
  }

  fun getCaseDtos(
    caseToUserAccess: Map<Case, UserAccess?>,
    searchTerm: String?,
    riskLevel: RiskLevel?,
    teamCode: String?,
    peopleType: PeopleType?,
    username: String?,
  ): List<CaseDto> {
    val crns = caseToUserAccess.keys.map { it.crn }
    val caseEntitiesByCrn = caseRepository.mapByCrns(crns)

    return caseToUserAccess.map { (case, userAccess) ->

      // entities are persisted in the previous step.
      val entity = caseEntitiesByCrn[case.crn]!!

      when (userAccess) {
        UserAccess.FULL -> {
          toCaseDto(
            caseEntity = entity,
            crn = entity.latestCrn(),
            prisonNumber = entity.latestPrisonNumber(),
            riskLevel = entity.roshLevelCode?.let { RiskLevel.findByCode(it) },
            pncReference = null,
            assignedTo = entity.toAssignedToDto(),
            limitedAccess = case.limitedAccess,
            teamCode = case.team.code,
          )
        }

        else -> toLimitedCaseDto(
          case.crn,
          teamCode = case.team.code,
          assignedTo = entity.toAssignedToDto(),
        )
      }
    }.filterCases(
      searchTerm = searchTerm,
      riskLevel = riskLevel,
      teamCode = teamCode,
      username = username,
      peopleType = peopleType,
    )
      .sortCases(LocalDate.now(clock))
  }

  fun getPersistedCase(crn: String) = caseRepository.findByCrn(crn)

  fun getCase(crn: String): ApiResponseDto<CaseDto> {
    val user = userService.authorizeAndRetrieveUser()
    val orchestrationResult = caseOrchestrationService.getCase(user.username, crn)
    hasMandatoryCaseData(orchestrationResult)
    limitedAccessService.throwIfUserAccessFailed(orchestrationResult.upstreamFailures)
    val crnToCaseAccess = limitedAccessService.getCrnToUserAccess(orchestrationResult.data.caseAccess)
    val case = orchestrationResult.data.case ?: throw IllegalStateException("CRN $crn not found")

    val caseOrchestrationDto = orchestrationResult.data
    val data = toCaseDto(
      crn,
      case,
      caseOrchestrationDto.cpr,
      caseOrchestrationDto.tier,
      caseOrchestrationDto.sexualOffenceRegistrations,
      crnToUserAccess = crnToCaseAccess,
    ).obfuscateIfLimitedAccess()
    return toApiResponseDto(data = data, upstreamFailures = orchestrationResult.upstreamFailures)
  }

  private fun hasMandatoryCaseData(orchestrationResult: OrchestrationResultDto<CaseOrchestrationDto>) {
    listOf(GET_CASE, GET_CORE_PERSON_RECORD_BY_CRN).forEach { key ->
      orchestrationResult.upstreamFailures.firstOrNull { it.callKey == key }?.let {
        throw UpstreamFailureException(UpstreamFailureTransformer.toUpstreamFailureDto(it))
      }
    }
  }

  fun getCaseFromDelius(crn: String): ApiResponseDto<Map<Case, UserAccess>> {
    val user = userService.authorizeAndRetrieveUser()
    val orchestrationResult = caseOrchestrationService.getCaseFromDelius(user.username, crn)
    hasMandatoryCaseData(orchestrationResult)
    limitedAccessService.throwIfUserAccessFailed(orchestrationResult.upstreamFailures)
    val case = orchestrationResult.data.case ?: throw IllegalArgumentException("CRN $crn not found in delius")

    val userAccess =
      limitedAccessService.getCrnToUserAccess(orchestrationResult.data.caseAccess)[crn] ?: UserAccess.UNKNOWN
    return toApiResponseDto(
      data = mapOf(case to userAccess),
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }
}
