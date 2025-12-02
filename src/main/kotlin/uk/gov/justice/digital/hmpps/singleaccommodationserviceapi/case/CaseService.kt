package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel

@Service
class CaseService(
  val caseOrchestrationService: CaseOrchestrationService,
) {
  fun getCases(crns: List<String>, riskLevel: RiskLevel?): List<CaseDto> = caseOrchestrationService.getCases(crns).let {
    it.map { caseOrchestrationDto ->
      val caseAggregate = CaseAggregate.hydrate(caseOrchestrationDto)
      caseAggregate.getCaseDto()
    }.filter { riskLevel == null || it.riskLevel == riskLevel }
  }

  fun getCase(crn: String): CaseDto {
    val caseOrchestrationDto = caseOrchestrationService.getCase(crn)
    val caseAggregate = CaseAggregate.hydrate(caseOrchestrationDto)
    return caseAggregate.getCaseDto()
  }
}
