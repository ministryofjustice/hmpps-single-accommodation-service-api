package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RiskLevel as RiskLevelInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore as TierScoreInfra

@Service
class CaseService(
  val caseOrchestrationService: CaseOrchestrationService,
) {
  fun getCases(crns: List<String>, riskLevel: RiskLevel?): List<CaseDto> {
    val list = caseOrchestrationService.getCases(crns)
    return list.map {
      CaseDto.from(
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
    return CaseDto.from(crn, case.cpr, case.roshDetails, case.tier, case.cases)
  }

  fun TierScoreInfra.toTierScore() = TierScore.valueOf(this.name)
  fun RiskLevelInfra.toRiskLevel() = RiskLevel.valueOf(this.name)

  fun CaseDto.Companion.from(
    crn: String,
    cpr: CorePersonRecord,
    roshDetails: RoshDetails,
    tier: Tier,
    caseSummaries: List<CaseSummary>,
  ) = CaseDto(
    name = cpr.fullName,
    dateOfBirth = cpr.dateOfBirth,
    crn = crn,
    prisonNumber = cpr.identifiers?.prisonNumbers?.firstOrNull(),
    tier = tier.tierScore.toTierScore(),
    riskLevel = roshDetails.rosh.determineOverallRiskLevel()?.toRiskLevel(),
    pncReference = cpr.identifiers?.pncs?.firstOrNull(),
    assignedTo = caseSummaries.firstOrNull()?.manager?.team?.name?.let {
      AssignedToDto(1L, name = it)
    },
    photoUrl = null,
    currentAccommodation = null,
    nextAccommodation = null,
  )
}
