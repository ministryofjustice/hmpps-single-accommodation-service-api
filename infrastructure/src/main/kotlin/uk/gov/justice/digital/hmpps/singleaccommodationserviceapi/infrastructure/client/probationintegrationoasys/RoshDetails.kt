package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys

import java.time.ZonedDateTime

data class RoshDetails(
  val assessmentId: Long,
  val assessmentType: String,
  val dateCompleted: ZonedDateTime? = null,
  val assessorSignedDate: ZonedDateTime? = null,
  val initiationDate: ZonedDateTime,
  val assessmentStatus: String,
  val superStatus: String? = null,
  val laterWIPAssessmentExists: Boolean? = null,
  val limitedAccessOffender: Boolean,
  val lastUpdatedDate: ZonedDateTime? = null,
  val rosh: Rosh,
)

data class Rosh(
  val riskChildrenCommunity: RiskLevel? = null,
  val riskPrisonersCustody: RiskLevel? = null,
  val riskStaffCustody: RiskLevel? = null,
  val riskStaffCommunity: RiskLevel? = null,
  val riskKnownAdultCustody: RiskLevel? = null,
  val riskKnownAdultCommunity: RiskLevel? = null,
  val riskPublicCustody: RiskLevel? = null,
  val riskPublicCommunity: RiskLevel? = null,
  val riskChildrenCustody: RiskLevel? = null,
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

    return levels.filter { it != null }.maxByOrNull { it!!.priority }
  }
}

enum class RiskLevel(val priority: Int) {
  LOW(1),
  MEDIUM(2),
  HIGH(3),
  VERY_HIGH(4),
}
