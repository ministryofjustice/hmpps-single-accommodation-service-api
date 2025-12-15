package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier

import java.time.LocalDateTime
import java.util.UUID

data class Tier(
  val tierScore: TierScore,
  val calculationId: UUID,
  val calculationDate: LocalDateTime,
  val changeReason: String?,
)

enum class TierScore {
  A3,
  A2,
  A1,
  B3,
  B2,
  B1,
  C3,
  C2,
  C1,
  D3,
  D2,
  D1,
  A3S,
  A2S,
  A1S,
  B3S,
  B2S,
  B1S,
  C3S,
  C2S,
  C1S,
  D3S,
  D2S,
  D1S,
}
