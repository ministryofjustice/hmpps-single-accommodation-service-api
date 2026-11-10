package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import java.time.LocalDateTime
import java.util.UUID

data class Tier(
  val tierScore: TierScore,
  val calculationId: UUID,
  val calculationDate: LocalDateTime,
  val changeReason: String?,
  val provisional: Boolean? = null,
)
