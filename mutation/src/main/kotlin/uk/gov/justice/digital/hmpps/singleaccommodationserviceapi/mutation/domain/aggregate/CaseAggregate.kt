package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import java.time.LocalDate
import java.util.UUID

class CaseAggregate private constructor(
  private val id: UUID,
  private var tierScore: String? = null,
  private var firstName: String? = null,
  private var lastName: String? = null,
  private var dateOfBirth: LocalDate? = null,
  private var hasSyncedCprProposedAccommodation: Boolean = false,
) {

  fun upsertCase(
    tierScore: String?,
    firstName: String? = null,
    lastName: String? = null,
    dateOfBirth: LocalDate? = null,
  ): CaseAggregate {
    updateTier(tierScore)
    this.firstName = firstName
    this.lastName = lastName
    this.dateOfBirth = dateOfBirth
    return this
  }

  companion object {
    fun hydrate(
      id: UUID,
      tierScore: String?,
      hasSyncedCprProposedAccommodation: Boolean,
      firstName: String? = null,
      lastName: String? = null,
      dateOfBirth: LocalDate? = null,
    ) = CaseAggregate(
      id = id,
      tierScore = tierScore,
      hasSyncedCprProposedAccommodation = hasSyncedCprProposedAccommodation,
      firstName = firstName,
      lastName = lastName,
      dateOfBirth = dateOfBirth,
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
    val firstName: String?,
    val lastName: String?,
    val dateOfBirth: LocalDate?,
  )

  fun snapshot() = CaseSnapshot(
    id,
    tierScore,
    hasSyncedCprProposedAccommodation,
    firstName,
    lastName,
    dateOfBirth,
  )
}
