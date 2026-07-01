package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier

import java.time.LocalDateTime
import java.util.UUID

data class Tier(
  val tierScore: String,
  val calculationId: UUID,
  val calculationDate: LocalDateTime,
  val changeReason: String?,
  val provisional: Boolean? = null,
)
