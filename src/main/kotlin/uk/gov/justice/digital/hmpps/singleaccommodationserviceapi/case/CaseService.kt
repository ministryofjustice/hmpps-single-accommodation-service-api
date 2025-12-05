package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel

@Service
class CaseService(
  val caseOrchestrationService: CaseOrchestrationService,
) {
  fun getCases(crns: List<String>, riskLevel: RiskLevel?) = caseOrchestrationService.getCases(crns).let {
    it.map { caseOrchestrationDto ->
      val caseAggregate = CaseAggregate.hydrate(caseOrchestrationDto)
      caseAggregate.getCaseDto()
    }
      .filter { caseDto -> riskLevel == null || caseDto.riskLevel == riskLevel }
      .sortedBy { caseDto -> caseDto.name }
  }

  fun getCase(crn: String) = caseOrchestrationService.getCase(crn).let {
    val caseAggregate = CaseAggregate.hydrate(it)
    caseAggregate.getCaseDto()
  }
}
