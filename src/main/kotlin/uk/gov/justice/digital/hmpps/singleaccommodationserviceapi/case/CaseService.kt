package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel

@Service
class CaseService(
  val caseOrchestrationService: CaseOrchestrationService,
) {
  fun getCases(crns: List<String>, riskLevel: RiskLevel?): List<CaseDto> {
    val list = caseOrchestrationService.getCases(crns)
    return list.map {
      CaseDto(
        crn = it.crn,
        cpr = it.cpr,
        roshDetails = it.roshDetails,
        tier = it.tier,
        caseSummaries = it.cases,
        photoUrl = it.photoUrl,
        accommodationDto = it.accommodationDto,
      )
    }
      .filter { riskLevel == null || it.riskLevel == riskLevel }
      .sortedBy { it.name }
  }

  fun getCase(crn: String): CaseDto {
    val case = caseOrchestrationService.getCase(crn)
    return CaseDto(crn, case.cpr, case.roshDetails, case.tier, case.cases, case.accommodationDto, case.photoUrl)
  }
}
