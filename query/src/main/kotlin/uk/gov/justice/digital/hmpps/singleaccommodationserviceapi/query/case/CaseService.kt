package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService

@Service
class CaseService(
  private val caseOrchestrationService: CaseOrchestrationService,
  private val eligibilityService: EligibilityService,

) {
  fun getCases(crns: List<String>, riskLevel: RiskLevel?): List<CaseDto> {
    val list = caseOrchestrationService.getCases(crns)
    return list.map {
      val eligibility = eligibilityService.getBulkEligibility(it)

      CaseTransformer.toCaseDto(
        crn = it.crn,
        cpr = it.cpr,
        roshDetails = it.roshDetails,
        tier = it.tier,
        caseSummaries = it.cases,
        eligibility = eligibility,
      )
    }
      .filter { riskLevel == null || it.riskLevel == riskLevel }
      .sortedBy { it.name }
  }

  fun getCase(crn: String): CaseDto {
    val case = caseOrchestrationService.getCase(crn)
    val eligibility = eligibilityService.getSingleEligibility(crn)
    return CaseTransformer.toCaseDto(crn, case.cpr, case.roshDetails, case.tier, case.cases, eligibility)
  }
}
