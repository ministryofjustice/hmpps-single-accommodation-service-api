package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import java.time.LocalDate
import java.util.UUID

class CaseAggregate private constructor(
  private val id: UUID,
  private var tierScore: String? = null,
  private var firstName: String? = null,
  private var lastName: String? = null,
  private var dateOfBirth: LocalDate? = null,
  private var hasSyncedCprProposedAccommodation: Boolean = false,
  private var currentAccommodation: AccommodationSummaryDto? = null,
  private var nextAccommodation: AccommodationSummaryDto? = null,
  private var accommodationStatus: CaseAccommodationStatus? = null,
  private var accommodationStatusDate: LocalDate? = null,
  private var roshLevelCode: String? = null,
  private var assignedToForename: String? = null,
  private var assignedToSurname: String? = null,
  private var assignedToUsername: String? = null,
) {

  fun upsertCase(
    tierScore: String?,
    firstName: String?,
    lastName: String?,
    dateOfBirth: LocalDate?,
    currentAccommodation: AccommodationSummaryDto?,
    nextAccommodation: AccommodationSummaryDto?,
    accommodationStatus: CaseAccommodationStatus?,
    accommodationStatusDate: LocalDate?,
    roshLevelCode: String?,
    assignedToForename: String?,
    assignedToSurname: String?,
    assignedToUsername: String?,
  ): CaseAggregate {
    updateTier(tierScore)
    this.firstName = firstName
    this.lastName = lastName
    this.dateOfBirth = dateOfBirth
    this.currentAccommodation = currentAccommodation
    this.nextAccommodation = nextAccommodation
    this.accommodationStatus = accommodationStatus
    this.accommodationStatusDate = accommodationStatusDate
    this.roshLevelCode = roshLevelCode
    this.assignedToForename = assignedToForename
    this.assignedToSurname = assignedToSurname
    this.assignedToUsername = assignedToUsername
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
      currentAccommodation: AccommodationSummaryDto? = null,
      nextAccommodation: AccommodationSummaryDto? = null,
      accommodationStatus: CaseAccommodationStatus? = null,
      accommodationStatusDate: LocalDate? = null,
      roshLevelCode: String? = null,
      assignedToForename: String? = null,
      assignedToSurname: String? = null,
      assignedToUsername: String? = null,
    ) = CaseAggregate(
      id = id,
      tierScore = tierScore,
      hasSyncedCprProposedAccommodation = hasSyncedCprProposedAccommodation,
      firstName = firstName,
      lastName = lastName,
      dateOfBirth = dateOfBirth,
      currentAccommodation = currentAccommodation,
      nextAccommodation = nextAccommodation,
      accommodationStatus = accommodationStatus,
      accommodationStatusDate = accommodationStatusDate,
      roshLevelCode = roshLevelCode,
      assignedToForename = assignedToForename,
      assignedToSurname = assignedToSurname,
      assignedToUsername = assignedToUsername,
    )

    fun hydrateNew(
      firstName: String? = null,
      lastName: String? = null,
      dateOfBirth: LocalDate? = null,
    ) = CaseAggregate(
      id = UUID.randomUUID(),
      firstName = firstName,
      lastName = lastName,
      dateOfBirth = dateOfBirth,
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
    val currentAccommodation: AccommodationSummaryDto?,
    val nextAccommodation: AccommodationSummaryDto?,
    val accommodationStatus: CaseAccommodationStatus?,
    val accommodationStatusDate: LocalDate?,
    val roshLevelCode: String?,
    val assignedToForename: String?,
    val assignedToSurname: String?,
    val assignedToUsername: String?,
  )

  fun snapshot() = CaseSnapshot(
    id,
    tierScore,
    hasSyncedCprProposedAccommodation,
    firstName,
    lastName,
    dateOfBirth,
    currentAccommodation,
    nextAccommodation,
    accommodationStatus,
    accommodationStatusDate,
    roshLevelCode,
    assignedToForename,
    assignedToSurname,
    assignedToUsername,
  )
}
