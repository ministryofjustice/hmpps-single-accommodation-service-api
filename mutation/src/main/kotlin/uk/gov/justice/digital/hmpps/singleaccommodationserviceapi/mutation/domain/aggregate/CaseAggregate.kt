package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.utils.RootAggregateHydrateFunction
import java.util.UUID

class CaseAggregate private constructor(
  private val id: UUID,
  private val crn: String,
  private var tier: TierScore? = null,
) {

  fun upsertTier(
    newTier: TierScore,
  ) {
    tier = newTier
  }

  companion object {
    @RootAggregateHydrateFunction
    fun hydrate(
      id: UUID,
      crn: String,
      tier: TierScore?,
    ) = CaseAggregate(
      id = id,
      crn = crn,
      tier = tier,
    )

    fun createNew(id: UUID, crn: String) = CaseAggregate(
      id = id,
      crn = crn,
    )
  }

  data class CaseSnapshot(
    val id: UUID,
    val crn: String,
    val tier: TierScore?,
  )

  fun snapshot() = CaseSnapshot(id, crn, tier)
}