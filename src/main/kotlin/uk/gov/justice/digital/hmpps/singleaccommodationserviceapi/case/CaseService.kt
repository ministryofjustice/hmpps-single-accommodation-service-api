package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.orchestration.CaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier
import java.time.LocalDate

@Service
class CaseService(
  val caseOrchestrationService: CaseOrchestrationService,
) {
  fun getCases(crns: List<String>, riskLevel: RiskLevel?): List<Case> {
    val list = caseOrchestrationService.getCases(crns)
    return list.map {
      buildCase(it.crn, it.cpr, it.roshDetails, it.tier, it.cases[0])
    }.filter { riskLevel == null || it.riskLevel == riskLevel }
  }

  fun getCase(crn: String): Case {
    val case = caseOrchestrationService.getCase(crn)
    return buildCase(case.crn, case.cpr, case.roshDetails, case.tier, case.cases[0])
  }

  fun buildCase(crn: String, cpr: CorePersonRecord, roshDetails: RoshDetails, tier: Tier, caseSummary: CaseSummary) = Case(
    name = cpr.fullName,
    dateOfBirth = caseSummary.dateOfBirth,
    crn = crn,
    prisonNumber = cpr.identifiers?.prisonNumbers[0],
    tier = tier.tierScore,
    riskLevel = roshDetails.rosh.determineOverallRiskLevel(),
    pncReference = caseSummary.pnc,
    assignedTo = AssignedTo(1L, caseSummary.manager.team.name),
    currentAccommodation = CurrentAccommodation("AIRBNB", LocalDate.now().plusDays(10)),
    nextAccommodation = NextAccommodation("PRISON", LocalDate.now().plusDays(100)),
  )
}
