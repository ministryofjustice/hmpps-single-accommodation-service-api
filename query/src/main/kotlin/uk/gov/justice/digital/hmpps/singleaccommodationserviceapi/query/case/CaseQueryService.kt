package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.UpstreamFailureException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailureTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_USER_ACCESS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toLimitedCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import java.time.Clock

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
    hasUserAccessData(caseOrchestrationResult.upstreamFailures)

    val cases = caseOrchestrationResult.data.cases
    val limitedAccessCrns = limitedAccessService.getLimitedAccessCrns(cases.map { it.crn }, caseOrchestrationResult.data.caseAccess)
    val caseList = cases.map { toPersonDto(it, limited = it.crn in limitedAccessCrns) }
    return toApiResponseDto(
      data = caseList,
      upstreamFailures = caseOrchestrationResult.upstreamFailures,
    )
  }

  fun applyCaseListFilters(
    personDtos: List<PersonDto>,
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
    }
      .filtered(peopleType)
      .sortCases(LocalDate.now(clock))
  }
  fun getPersistedCase(crn: String) = caseRepository.findByCrn(crn)

  fun getCase(crn: String): ApiResponseDto<CaseDto> {
    val user = userService.authorizeAndRetrieveUser()
    val orchestrationResult = caseOrchestrationService.getCase(user.username, crn)
    hasMandatoryCaseData(orchestrationResult)
    val case = orchestrationResult.data.case?.let { toPersonDto(it, limited = it.hasExclusionOrRestriction()) }

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

  private fun hasUserAccessData(upstreamFailures: List<UpstreamFailure>) {
    upstreamFailures.firstOrNull { it.callKey.startsWith(GET_USER_ACCESS) }?.let {
      throw UpstreamFailureException(UpstreamFailureTransformer.toUpstreamFailureDto(it))
    }
  }

  fun getCaseFromDelius(crn: String): ApiResponseDto<PersonDto?> {
    val user = userService.authorizeAndRetrieveUser()
    val orchestrationResult = caseOrchestrationService.getCaseFromDelius(user.username, crn)
    hasMandatoryCaseData(orchestrationResult)
    val case = orchestrationResult.data.case?.let { toPersonDto(it, limited = it.hasExclusionOrRestriction()) }
    return toApiResponseDto(data = case, upstreamFailures = orchestrationResult.upstreamFailures)
  }

  private fun Case.hasExclusionOrRestriction() = userExcluded == true || userRestricted == true
}
