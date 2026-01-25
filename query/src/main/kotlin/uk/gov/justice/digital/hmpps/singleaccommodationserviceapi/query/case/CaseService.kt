package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel

@Service
class CaseService(
  private val caseOrchestrationService: CaseOrchestrationService,
) {
  fun getCases(crns: List<String>, riskLevel: RiskLevel?): List<CaseDto> {
    val list = caseOrchestrationService.getCases(crns)
    return list.map {
      CaseTransformer.toCaseDto(
        crn = it.crn,
        cpr = it.cpr,
        roshDetails = it.roshDetails,
        tier = it.tier,
        caseSummaries = it.cases
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
