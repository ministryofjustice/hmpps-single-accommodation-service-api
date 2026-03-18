package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto

@Service
class CaseQueryService(
  private val caseOrchestrationService: CaseOrchestrationService,
  private val userService: UserService,
  private val caseRepository: CaseRepository,
  private val eligibilityService: EligibilityService,
) {
  fun getCaseList(): List<PersonDto> {
    val user = userService.authorizeAndRetrieveUser()

    // new pi endpoint
    val caseList = caseOrchestrationService.getCaseList(user.username)

    return caseList.cases.map { toPersonDto(it) }
  }

  fun getCases(personDtos: List<PersonDto>): List<CaseDto> {
    val crns = personDtos.map { it.crn }
    val caseEntities = caseRepository.findByCrns(crns)

    return personDtos.map { personDto ->
      val caseEntity = caseEntities.find { entity ->
        entity.caseIdentifiers.any { it.identifier == personDto.crn && it.identifierType == IdentifierType.CRN }
      }
      val eligibilityDto = eligibilityService.getEligibility(personDto, caseEntity)
      toCaseDto(
        personDto,
        caseEntity,
        eligibilityDto,
      )
    }
  }

  fun getCases(crns: List<String>, riskLevel: RiskLevel?): List<CaseDto> {
    val list = caseOrchestrationService.getCases(crns)
    return list.map {
      toCaseDto(
        crn = it.crn,
        cpr = it.cpr,
        roshDetails = it.roshDetails,
        tier = it.tier,
        caseSummaries = it.cases,
      )
    }
      .filter { riskLevel == null || it.riskLevel == riskLevel }
      .sortedBy { it.name }
  }

  fun getCase(crn: String): CaseDto {
    val case = caseOrchestrationService.getCase(crn)
    return toCaseDto(crn, case.cpr, case.roshDetails, case.tier, case.cases)
  }

  fun getCasesV2(crns: List<String>, riskLevel: RiskLevel?): ApiResponseDto<List<CaseDto>> {
    val orchestrationResult = caseOrchestrationService.getCasesV2(crns)
    val cases = orchestrationResult.data.map {
      toCaseDto(
        crn = it.crn,
        cpr = it.cpr,
        roshDetails = it.roshDetails,
        tier = it.tier,
        caseSummaries = it.cases,
      )
    }
      .filter { riskLevel == null || it.riskLevel == riskLevel }
      .sortedBy { it.name }

    return toApiResponseDto(
      data = cases,
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  fun getCaseV2(crn: String): ApiResponseDto<CaseDto> {
    val orchestrationResult = caseOrchestrationService.getCaseV2(crn)
    val case = orchestrationResult.data
    return toApiResponseDto(
      data = toCaseDto(crn, case.cpr, case.roshDetails, case.tier, case.cases),
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }
}
