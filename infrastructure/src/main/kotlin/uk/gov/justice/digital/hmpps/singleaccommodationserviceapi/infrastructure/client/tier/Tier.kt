package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier

import java.time.LocalDateTime
import java.util.UUID

data class Tier(
  val tierScore: String,
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
    fun placeholder(tierScore: String): Tier = Tier(
      tierScore = tierScore,
      calculationId = UUID.randomUUID(),
      calculationDate = LocalDateTime.now(),
      changeReason = null,
    )
  }
}
