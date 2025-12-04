package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated

import java.time.Instant
import java.util.UUID

// TODO swap this out for the generated model

data class Cas2ReferralHistory(
  val type: ServiceType,
  val id: UUID,
  val applicationId: UUID,
  val status: String,
  val createdAt: Instant,
)
