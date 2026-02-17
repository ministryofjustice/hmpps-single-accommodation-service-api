package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier

import java.time.LocalDateTime
import java.util.UUID

data class Tier(
  val tierScore: TierScore,
  val calculationId: UUID,
  val calculationDate: LocalDateTime,
  val changeReason: String?,
) {
  /**
   * Creates a placeholder Tier from a tier score when full tier metadata (calculationId,
   * calculationDate, changeReason) is not available, e.g. when reading from the cases table.
   * This will be removed in subsequent code changes
   */
  companion object {
    fun placeholder(tierScore: TierScore): Tier =
      Tier(
        tierScore = tierScore,
        calculationId = UUID.randomUUID(),
        calculationDate = LocalDateTime.now(),
        changeReason = null,
      )
  }
}

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
