package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys

import java.time.OffsetDateTime

data class RoshSummary(
  val assessmentId: Long,
  val assessmentType: String,
  val dateCompleted: OffsetDateTime?,
  val assessorSignedDate: OffsetDateTime?,
  val initiationDate: OffsetDateTime,
  val assessmentStatus: String,
  val superStatus: String?,
  val limitedAccessOffender: Boolean,
  val whoIsAtRisk: String?,
  val natureOfRisk: String?,
  val riskGreatest: String?,
  val riskIncreaseLikelyTo: String?,
  val riskReductionLikelyTo: String?,
  val factorsAnalysisOfRisk: String?,
  val factorsStrengthsAndProtective: String?,
  val factorsSituationsLikelyToOffend: String?,
)
