package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.Rosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RiskLevel as RiskLevelInfra

fun determineOverallRiskLevel(rosh: Rosh): RiskLevel? {
  val levels = listOf(
    rosh.riskChildrenCommunity,
    rosh.riskPrisonersCustody,
    rosh.riskStaffCommunity,
    rosh.riskStaffCustody,
    rosh.riskKnownAdultCommunity,
    rosh.riskKnownAdultCustody,
    rosh.riskPublicCommunity,
    rosh.riskPublicCustody,
  )
  val highestRiskLevel = levels.filter { it != null }.maxByOrNull { it!!.priority }
  return toRiskLevel(highestRiskLevel)
}

fun toRiskLevel(riskLevelInfra: RiskLevelInfra?) =
  if (riskLevelInfra != null) RiskLevel.valueOf(riskLevelInfra.name) else null
