package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService

@Service
class CaseService(
  private val caseOrchestrationService: CaseOrchestrationService,
  private val caseRepository: CaseRepository,
  private val eligibilityService: EligibilityService,
) {
  fun getCases(caseListItems: List<Case>): List<CaseDto> {
    val crns = caseListItems.map { it.crn }
    val caseEntities = caseRepository.findByCrns(crns)

    return caseListItems.map { caseListItem ->
      val caseEntity = caseEntities.find { caseListItem.crn == it.crn } ?: error("Case entity not found for CRN: ${caseListItem.crn}")
      val eligibilityDto = eligibilityService.getCachedEligibility(caseListItem, caseEntity)
      CaseTransformer.toCaseDto(
        caseListItem,
        caseEntity,
        eligibilityDto,
      )
    }
  }

  fun getCases(crns: List<String>, riskLevel: RiskLevel?): List<CaseDto> {
    val list = caseOrchestrationService.getCases(crns)
    return list.map {
      CaseTransformer.toCaseDto(
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
    return CaseTransformer.toCaseDto(crn, case.cpr, case.roshDetails, case.tier, case.cases)
  }
}
