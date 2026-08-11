package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import java.util.UUID

class CaseAggregate private constructor(
  private val id: UUID,
  private var tierScore: String? = null,
  private var hasSyncedCprProposedAccommodation: Boolean = false,
) {

  fun upsertCase(
    tierScore: String?,
  ): CaseAggregate {
    updateTier(tierScore)
    return this
  }

  companion object {
    fun hydrate(
      id: UUID,
      tierScore: String?,
      hasSyncedCprProposedAccommodation: Boolean,
    ) = CaseAggregate(
      id = id,
      tierScore = tierScore,
      hasSyncedCprProposedAccommodation = hasSyncedCprProposedAccommodation,
    )

    fun hydrateNew() = CaseAggregate(
      id = UUID.randomUUID(),
    )
  }

  fun updateTier(
    tierScore: String?,
  ) {
    this.tierScore = tierScore
  }

  fun markCaseAsSyncedWithCprProposedAccommodation() {
    hasSyncedCprProposedAccommodation = true
  }

  data class CaseSnapshot(
    val id: UUID,
    val tierScore: String?,
    val hasSyncedCprProposedAccommodation: Boolean,
  )

  fun snapshot() = CaseSnapshot(
    id,
    tierScore,
    hasSyncedCprProposedAccommodation,
  )
}
