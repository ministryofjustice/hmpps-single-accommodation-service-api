package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated

import java.time.Instant
import java.util.UUID

data class Cas3ReferralHistory(
  val type: ServiceType,
  val id: UUID,
  val applicationId: UUID,
  val status: TemporaryAccommodationAssessmentStatus,
  val createdAt: Instant,
)
