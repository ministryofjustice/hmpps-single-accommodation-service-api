package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto

@Service
class CaseQueryService(
  private val caseOrchestrationService: CaseOrchestrationService,
  private val userService: UserService,
  private val caseRepository: CaseRepository,
  private val eligibilityService: EligibilityService,
  private val dutyToReferQueryService: DutyToReferQueryService,
) {
  fun getCaseList(): List<PersonDto> {
    val user = userService.authorizeAndRetrieveUser()
    return caseOrchestrationService.getCaseList(user.username).cases.map { toPersonDto(it) }
  }

  private fun PersonDto.matchesTeam(teamCode: String?): Boolean = when {
    // TODO this will become .matchesUserOrTeam, however it's a significant refactor to test data setup, so will be in a following PR.
    teamCode.isNullOrBlank() -> true
    teamCode.equals(this.teamCode, ignoreCase = true) -> true
    else -> false
  }

  private fun PersonDto.matchesRosh(riskLevel: RiskLevel?): Boolean = when {
    riskLevel == null -> true
    this is Identifiable && roshLevel == riskLevel -> true
    else -> false
  }

  private fun PersonDto.matchesSearch(searchTerm: String?): Boolean = when {
    searchTerm.isNullOrBlank() -> true
    crn.trim().equals(searchTerm, true) -> true
    nomsNumber?.trim().equals(searchTerm, true) -> true
    this is Identifiable && name.contains(searchTerm, true) -> true
    else -> false
  }

  fun getCases(
    personDtos: List<PersonDto>,
    searchTerm: String? = null,
    riskLevel: RiskLevel? = null,
    teamCode: String? = null,
  ): List<CaseDto> {
    val filteredPersonDtos = personDtos
      .asSequence()
      .filter {
        it.matchesSearch(searchTerm) &&
          it.matchesRosh(riskLevel) &&
          it.matchesTeam(teamCode)
      }.toList()

    val crns = filteredPersonDtos.map { it.crn }

    val caseEntitiesByCrn = caseRepository.mapByCrns(crns)

    return filteredPersonDtos.map { personDto ->

      when (personDto) {
        is ExcludedPersonDto -> personDto.toCaseDto(caseEntity = null, eligibility = null)

        is RestrictedPersonDto, is FullPersonDto -> {
          val caseEntity = caseEntitiesByCrn[personDto.crn]
          val dutyToRefer = caseEntity?.let { dutyToReferQueryService.getDutyToRefer(it, personDto.crn) }
          val eligibility = eligibilityService.getEligibility(
            crn = personDto.crn,
            gender = personDto.gender,
            caseEntity = caseEntity,
            dutyToRefer = dutyToRefer,
          )
          personDto.toCaseDto(caseEntity = caseEntitiesByCrn[personDto.crn], eligibility = eligibility)
        }
      }
    }
  }

  fun getCase(crn: String): ApiResponseDto<CaseDto> {
    val user = userService.authorizeAndRetrieveUser()
    val orchestrationResult = caseOrchestrationService.getCase(user.username, crn)
    val case = orchestrationResult.data.case?.let { toPersonDto(it) }

    val caseOrchestrationDto = orchestrationResult.data
    val data = toCaseDto(
      crn,
      case,
      caseOrchestrationDto.cpr,
      caseOrchestrationDto.roshDetails,
      caseOrchestrationDto.tier,
    )
    return toApiResponseDto(data = data, upstreamFailures = orchestrationResult.upstreamFailures)
  }
}
