package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PeopleType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username
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
    searchTerm: String? = null,
    riskLevel: RiskLevel? = null,
    teamCode: String? = null,
  ) = personDtos
    .filter {
      if (!teamCode.isNullOrBlank()) {
        it.matchesTeam(teamCode)
      } else {
        it.matchesUser(userService.getUsername())
      } &&
        it.matchesSearch(searchTerm) &&
        it.matchesRiskLevel(riskLevel)
    }
    .sortedWith(
      compareBy<PersonDto>({ it !is Identifiable })
        .thenBy { (it as? Identifiable)?.surname?.lowercase() }
        .thenBy { (it as? Identifiable)?.forename?.lowercase() },
    )

  fun getCases(
    personDtos: List<PersonDto>,
    peopleType: PeopleType? = null,
  ): List<CaseDto> {
    val caseEntitiesByCrn = caseRepository.mapByCrns(personDtos.map { it.crn })
    val caseDtos = personDtos.map { personDto ->

      when (personDto) {
        is LimitedPersonDto -> personDto.toLimitedCaseDto()

        is FullPersonDto -> {
          val caseEntity = caseEntitiesByCrn[personDto.crn]
          personDto.toCaseDto(
            caseEntity = caseEntity,
            currentAccommodation = caseEntity?.currentAccommodation,
            nextAccommodation = caseEntity?.nextAccommodation,
          )
        }
      }
    }
      .sortCases(clock)
    return when (peopleType) {
      PeopleType.NFA_RISK ->
        caseDtos.filter { it.accommodationSummaries?.caseAccommodationStatus != CaseAccommodationStatus.SETTLED }
      PeopleType.HOUSED ->
        caseDtos.filter { it.accommodationSummaries?.caseAccommodationStatus == CaseAccommodationStatus.SETTLED }
      else -> caseDtos
    }
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

  private fun PersonDto.matchesUser(username: Username) = username.value.equals(this.assignedTo.username, ignoreCase = true)

  private fun PersonDto.matchesTeam(teamCode: String): Boolean = when {
    teamCode.equals(this.teamCode, ignoreCase = true) -> true
    else -> false
  }

  private fun PersonDto.matchesRiskLevel(riskLevel: RiskLevel?): Boolean = when {
    riskLevel == null -> true
    this is Identifiable && this.riskLevel == riskLevel -> true
    else -> false
  }

  private fun PersonDto.matchesSearch(searchTerm: String?): Boolean = when {
    searchTerm.isNullOrBlank() -> true
    crn.trim().equals(searchTerm, true) -> true
    nomsNumber?.trim().equals(searchTerm, true) -> true
    this is Identifiable -> {
      val fullName = "$forename ${middleNames ?: ""} $surname"
      searchTerm
        .split(" ")
        .filter { it.isNotBlank() }
        .all { fullName.contains(it, ignoreCase = true) }
    }
    else -> false
  }
}
