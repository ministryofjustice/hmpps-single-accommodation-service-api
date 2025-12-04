package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model
import java.time.Instant
import java.util.UUID

data class Cas2v2ReferralHistory(
  val type: ServiceType,
  val id: UUID,
  val applicationId: UUID,
  val status: String,
  val createdAt: Instant,
)
