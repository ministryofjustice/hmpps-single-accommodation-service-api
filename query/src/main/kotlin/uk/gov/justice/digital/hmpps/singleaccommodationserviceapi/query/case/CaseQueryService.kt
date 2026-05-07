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
  fun getCaseList(
    searchTerm: String? = null,
    riskLevel: RiskLevel? = null,
  ): List<PersonDto> {
    val user = userService.authorizeAndRetrieveUser()

    return caseOrchestrationService.getCaseList(user.username)
      .cases
      .asSequence()
      .map { toPersonDto(it) }
      .filter {
        it.matchesSearch(searchTerm) &&
          it.matchesRosh(riskLevel)
      }.toList()
  }

  private fun PersonDto.matchesRosh(riskLevel: RiskLevel?): Boolean = when {
    riskLevel == null -> true
    roshLevel == riskLevel -> true
    else -> false
  }

  private fun PersonDto.matchesSearch(searchTerm: String?): Boolean = when {
    searchTerm.isNullOrBlank() -> true
    crn.equals(searchTerm, true) -> true
    nomsNumber?.equals(searchTerm, true) == true -> true
    name.contains(searchTerm, true) -> true
    else -> false
  }

  fun getCases(personDtos: List<PersonDto>): List<CaseDto> {
    val crns = personDtos.map { it.crn }

    val caseEntitiesByCrn = caseRepository.mapByCrns(crns)

    return personDtos.map { personDto ->
      val caseEntity = caseEntitiesByCrn[personDto.crn]
      val dutyToRefer = caseEntity?.let { dutyToReferQueryService.getDutyToRefer(it, personDto.crn) }

      val eligibilityDto = eligibilityService.getEligibility(personDto, caseEntity, dutyToRefer)
      toCaseDto(
        personDto,
        caseEntity,
        eligibilityDto,
      )
    }
  }

  fun getCase(crn: String): ApiResponseDto<CaseDto> {
    val user = userService.authorizeAndRetrieveUser()
    val orchestrationResult = caseOrchestrationService.getCase(user.username, crn)
    val caseOrchestrationDto = orchestrationResult.data
    return toApiResponseDto(
      data = toCaseDto(
        crn,
        caseOrchestrationDto.cpr,
        caseOrchestrationDto.roshDetails,
        caseOrchestrationDto.tier,
        caseOrchestrationDto.case,
      ),
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }
}
