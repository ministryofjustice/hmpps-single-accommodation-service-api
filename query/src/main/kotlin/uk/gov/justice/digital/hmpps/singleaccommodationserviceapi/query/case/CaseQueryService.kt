package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PeopleType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.UpstreamFailureException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailureTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toLimitedCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto
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
  fun getCaseList(teamCode: String?): ApiResponseDto<List<PersonDto>> {
    val user = userService.authorizeAndRetrieveUser()
    val caseOrchestrationResult = caseOrchestrationService.getCaseList(user.username, teamCode)
    limitedAccessService.throwIfUserAccessFailed(caseOrchestrationResult.upstreamFailures)

    val cases = caseOrchestrationResult.data.cases
    val limitedAccessCrns = limitedAccessService.getLimitedAccessCrns(cases.map { it.crn }, caseOrchestrationResult.data.caseAccess)
    val caseList = cases.map { toPersonDto(it, limited = it.crn in limitedAccessCrns) }
    return toApiResponseDto(
      data = caseList,
      upstreamFailures = caseOrchestrationResult.upstreamFailures,
    )
  }

  fun getCaseDtos(
    personDtos: List<PersonDto>,
    searchTerm: String?,
    riskLevel: RiskLevel?,
    teamCode: String?,
    peopleType: PeopleType?,
    username: String?,
  ): List<CaseDto> {
    val caseEntitiesByCrn = caseRepository.mapByCrns(personDtos.map { it.crn })
    return personDtos.map { personDto ->

      when (personDto) {
        is LimitedPersonDto -> personDto.toLimitedCaseDto()

        is FullPersonDto -> {
          val caseEntity = caseEntitiesByCrn[personDto.crn]
          personDto.toCaseDto(
            caseEntity = caseEntity,
            currentAccommodation = caseEntity?.currentAccommodation,
            nextAccommodation = caseEntity?.nextAccommodation,
            teamCode = personDto.teamCode,
          )
        }
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
    val case = orchestrationResult.data.getPersonDto()

    val caseOrchestrationDto = orchestrationResult.data
    val data = toCaseDto(
      crn,
      case,
      caseOrchestrationDto.cpr,
      caseOrchestrationDto.tier,
      caseOrchestrationDto.sexualOffenceRegistrations,
    )
    return toApiResponseDto(data = data, upstreamFailures = orchestrationResult.upstreamFailures)
  }

  private fun hasMandatoryCaseData(orchestrationResult: OrchestrationResultDto<CaseOrchestrationDto>) {
    listOf(GET_CASE, GET_CORE_PERSON_RECORD_BY_CRN).forEach { key ->
      orchestrationResult.upstreamFailures.firstOrNull { it.callKey == key }?.let {
        throw UpstreamFailureException(UpstreamFailureTransformer.toUpstreamFailureDto(it))
      }
    }
  }

  fun getCaseFromDelius(crn: String): ApiResponseDto<PersonDto?> {
    val user = userService.authorizeAndRetrieveUser()
    val orchestrationResult = caseOrchestrationService.getCaseFromDelius(user.username, crn)
    hasMandatoryCaseData(orchestrationResult)
    limitedAccessService.throwIfUserAccessFailed(orchestrationResult.upstreamFailures)
    val case = orchestrationResult.data.getPersonDto()
    return toApiResponseDto(data = case, upstreamFailures = orchestrationResult.upstreamFailures)
  }

  private fun CaseOrchestrationDto.getPersonDto() = case?.let {
    toPersonDto(it, limited = crn in limitedAccessService.getLimitedAccessCrns(listOf(crn), caseAccess))
  }


  private fun Case.hasExclusionOrRestriction() = userExcluded == true || userRestricted == true
}
