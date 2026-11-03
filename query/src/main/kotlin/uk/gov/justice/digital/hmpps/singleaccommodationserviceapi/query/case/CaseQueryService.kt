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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toAssignedToDto
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

  fun getCaseList(teamCode: String?): ApiResponseDto<List<CaseWithUserAccess>> {
    val user = userService.authorizeAndRetrieveUser()
    val caseOrchestrationResult = caseOrchestrationService.getCaseList(user.username, teamCode)
    limitedAccessService.throwIfUserAccessFailed(caseOrchestrationResult.upstreamFailures)

    val crnsToUserAccess = limitedAccessService.getCrnToUserAccess(caseOrchestrationResult.data.caseAccess)
    val cases = caseOrchestrationResult.data.cases.map { CaseWithUserAccess(it, crnsToUserAccess[it.crn]) }

    return toApiResponseDto(
      data = cases,
      upstreamFailures = caseOrchestrationResult.upstreamFailures,
    )
  }

  fun getCaseDtos(
    cases: List<CaseWithUserAccess>,
    searchTerm: String?,
    riskLevel: RiskLevel?,
    teamCode: String?,
    peopleType: PeopleType?,
    username: String?,
  ): List<CaseDto> {
    val caseEntitiesByCrn = caseRepository.mapByCrns(cases.map { it.case.crn })

    return cases.map { (case, userAccess) ->
      val entity = caseEntitiesByCrn[case.crn]
      // fallback for if a case row has no assignedTo - use Delius data to ensure case is not hidden
      val assignedTo = entity?.toAssignedToDto() ?: case.toAssignedToDto()

      when (userAccess) {
        UserAccess.FULL -> {
          toCaseDto(
            caseEntity = entity,
            crn = case.crn,
            prisonNumber = entity?.latestPrisonNumber(),
            riskLevel = entity?.roshLevelCode?.let { RiskLevel.findByCode(it) },
            assignedTo = assignedTo,
            limitedAccess = case.limitedAccess ?: false,
            teamCode = case.team.code,
          )
        }

        UserAccess.LIMITED, UserAccess.UNKNOWN, null -> toLimitedCaseDto(
          case.crn,
          teamCode = case.team.code,
          assignedTo = assignedTo,
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

    val caseOrchestrationDto = orchestrationResult.data
    val data = toCaseDto(
      crn,
      caseOrchestrationDto.case,
      caseOrchestrationDto.cpr,
      caseOrchestrationDto.tier,
      caseOrchestrationDto.sexualOffenceRegistrations,
      caseOrchestrationDto.getUserAccess(),
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

  fun getCaseFromDelius(crn: String): ApiResponseDto<CaseWithUserAccess?> {
    val user = userService.authorizeAndRetrieveUser()
    val orchestrationResult = caseOrchestrationService.getCaseFromDelius(user.username, crn)
    hasMandatoryCaseData(orchestrationResult)
    limitedAccessService.throwIfUserAccessFailed(orchestrationResult.upstreamFailures)
    val caseOrchestrationDto = orchestrationResult.data
    val case = caseOrchestrationDto.case?.let { CaseWithUserAccess(it, caseOrchestrationDto.getUserAccess()) }
    return toApiResponseDto(data = case, upstreamFailures = orchestrationResult.upstreamFailures)
  }

  private fun CaseOrchestrationDto.getUserAccess() = case?.let { limitedAccessService.getCrnToUserAccess(caseAccess)[it.crn] }
}

data class CaseWithUserAccess(val case: Case, val userAccess: UserAccess?)
