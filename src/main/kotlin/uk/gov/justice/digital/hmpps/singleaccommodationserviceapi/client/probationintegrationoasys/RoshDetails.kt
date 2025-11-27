package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys

import java.time.Instant

data class RoshDetails(
  val assessmentId: Int,
  val assessmentType: String,
  val dateCompleted: Instant,
  val assessorSignedDate: Instant,
  val initiationDate: Instant,
  val assessmentStatus: String,
  val superStatus: String,
  val laterWIPAssessmentExists: Boolean,
  val limitedAccessOffender: Boolean,
  val lastUpdatedDate: Instant,
  val rosh: Rosh,
)

data class Rosh(
  val riskChildrenCommunity: RiskLevel,
  val riskPrisonersCustody: RiskLevel,
  val riskStaffCustody: RiskLevel,
  val riskStaffCommunity: RiskLevel,
  val riskKnownAdultCustody: RiskLevel,
  val riskKnownAdultCommunity: RiskLevel,
  val riskPublicCustody: RiskLevel,
  val riskPublicCommunity: RiskLevel,
  val riskChildrenCustody: RiskLevel,
) {
  fun determineOverallRiskLevel(): RiskLevel? {
    val levels = listOf(
      riskChildrenCommunity,
      riskPrisonersCustody,
      riskStaffCommunity,
      riskStaffCustody,
      riskKnownAdultCommunity,
      riskKnownAdultCustody,
      riskPublicCommunity,
      riskPublicCustody,
    )

    return levels.maxByOrNull { it.priority }
  }
}

enum class RiskLevel(val priority: Int) {
  LOW(1),
  MEDIUM(2),
  HIGH(3),
  VERY_HIGH(4),
}
