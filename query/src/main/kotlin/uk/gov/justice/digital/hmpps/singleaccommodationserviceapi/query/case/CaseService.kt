package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel

@Service
class CaseService(
  private val caseOrchestrationService: CaseOrchestrationService,
) {
  fun getCases(crns: List<String>, riskLevel: RiskLevel?): List<CaseDto> {
    val results = caseOrchestrationService.getCases(crns)
    return results.map {
      CaseTransformer.toCaseDto(
        crn = it.data.crn,
        cpr = it.data.cpr,
        roshDetails = it.data.roshDetails,
        tier = it.data.tier,
        caseSummaries = it.data.cases,
        upstreamFailures = it.upstreamFailures,
      )
    }
      .filter { riskLevel == null || it.riskLevel == riskLevel }
      .sortedBy { it.name }
  }

  fun getCase(crn: String): CaseDto {
    val result = caseOrchestrationService.getCase(crn)
    return CaseTransformer.toCaseDto(
      crn = result.data.crn,
      cpr = result.data.cpr,
      roshDetails = result.data.roshDetails,
      tier = result.data.tier,
      caseSummaries = result.data.cases,
      upstreamFailures = result.upstreamFailures,
    )
  }
}
