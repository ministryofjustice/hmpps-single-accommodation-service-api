package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralOutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsEmptyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsGreaterThanMaxLengthException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.OtherAccommodationReferralOutcomeNoteNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.OtherAccommodationReferralOutcomeReasonNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.OtherAccommodationReferralOutcomeReasonRequiredException
import java.time.LocalDate
import java.util.UUID

private const val NOTE_MAX_LENGTH = 4000

private val ACCEPTED_OUTCOME_REASONS = setOf(
  OtherAccommodationReferralOutcomeReason.ACCEPTED_BY_ORGANISATION,
  OtherAccommodationReferralOutcomeReason.ACCEPTED_WITH_ACCOMMODATION_PLACEMENT,
)
private val REJECTED_OUTCOME_REASONS = setOf(
  OtherAccommodationReferralOutcomeReason.PERSON_NOT_SUITABLE,
  OtherAccommodationReferralOutcomeReason.NO_CAPACITY,
  OtherAccommodationReferralOutcomeReason.ANOTHER_REASON,
)

class OtherAccommodationReferralAggregate private constructor(
  private val id: UUID,
  private val caseId: UUID,
  private val crn: String,
  private var referenceNumber: String? = null,
  private var submissionDate: LocalDate? = null,
  private var status: OtherAccommodationReferralStatus? = null,
  private var organisationName: String? = null,
  private var website: String? = null,
  private var submissionNote: String? = null,
  private var outcomeReason: OtherAccommodationReferralOutcomeReason? = null,
  private var outcomeNote: String? = null,
  private var notes: MutableList<OtherAccommodationReferralNote> = mutableListOf(),
) {
  companion object {
    fun hydrateNew(caseId: UUID, crn: String) = OtherAccommodationReferralAggregate(
      id = UUID.randomUUID(),
      caseId = caseId,
      crn = crn,
    )

    fun hydrateExisting(
      id: UUID,
      caseId: UUID,
      crn: String,
      referenceNumber: String?,
      submissionDate: LocalDate,
      status: OtherAccommodationReferralStatus,
      organisationName: String?,
      website: String?,
      submissionNote: String?,
      notes: List<OtherAccommodationReferralNote>,
      outcomeReason: OtherAccommodationReferralOutcomeReason? = null,
      outcomeNote: String? = null,
    ) = OtherAccommodationReferralAggregate(
      id = id,
      caseId = caseId,
      crn = crn,
      referenceNumber = referenceNumber,
      submissionDate = submissionDate,
      status = status,
      organisationName = organisationName,
      website = website,
      submissionNote = submissionNote,
      outcomeReason = outcomeReason,
      outcomeNote = outcomeNote,
      notes = notes.toMutableList(),
    )
  }

  fun addNote(note: String) {
    validateNote(note)
    notes += OtherAccommodationReferralNote(
      id = UUID.randomUUID(),
      note = note,
    )
  }

  private fun validateNote(note: String) {
    if (note.isBlank()) {
      throw NoteIsEmptyException()
    }
    validateNoteLength(note)
  }

  private fun validateNoteLength(note: String) {
    if (note.length > NOTE_MAX_LENGTH) {
      throw NoteIsGreaterThanMaxLengthException()
    }
  }

  fun updateOtherAccommodationReferral(
    submissionDate: LocalDate,
    referenceNumber: String?,
    status: OtherAccommodationReferralStatus,
    organisationName: String?,
    website: String?,
    submissionNote: String?,
    outcomeReason: OtherAccommodationReferralOutcomeReason? = null,
    outcomeNote: String? = null,
  ) {
    validateOutcome(status, outcomeReason, outcomeNote)

    this.submissionDate = submissionDate
    this.referenceNumber = referenceNumber
    this.status = status
    this.organisationName = organisationName
    this.website = website
    this.submissionNote = submissionNote?.takeUnless { it.isBlank() }

    if (status == OtherAccommodationReferralStatus.ACCEPTED || status == OtherAccommodationReferralStatus.REJECTED) {
      this.outcomeReason = outcomeReason
      this.outcomeNote = outcomeNote?.takeUnless { it.isBlank() }?.also { validateNoteLength(it) }
    } else {
      this.outcomeReason = null
      this.outcomeNote = null
    }
  }

  private fun validateOutcome(
    status: OtherAccommodationReferralStatus,
    outcomeReason: OtherAccommodationReferralOutcomeReason?,
    outcomeNote: String?,
  ) {
    when (status) {
      OtherAccommodationReferralStatus.ACCEPTED -> validateOutcomeReason(outcomeReason, ACCEPTED_OUTCOME_REASONS)
      OtherAccommodationReferralStatus.REJECTED -> validateOutcomeReason(outcomeReason, REJECTED_OUTCOME_REASONS)
      OtherAccommodationReferralStatus.SUBMITTED -> validateNoOutcome(outcomeReason, outcomeNote)
    }
  }

  private fun validateOutcomeReason(
    outcomeReason: OtherAccommodationReferralOutcomeReason?,
    validReasons: Set<OtherAccommodationReferralOutcomeReason>,
  ) {
    when {
      outcomeReason == null ->
        throw OtherAccommodationReferralOutcomeReasonRequiredException()

      outcomeReason !in validReasons ->
        throw OtherAccommodationReferralOutcomeReasonNotApplicableException()
    }
  }

  private fun validateNoOutcome(
    outcomeReason: OtherAccommodationReferralOutcomeReason?,
    outcomeNote: String?,
  ) {
    if (outcomeReason != null) {
      throw OtherAccommodationReferralOutcomeReasonNotApplicableException()
    }

    if (!outcomeNote.isNullOrBlank()) {
      throw OtherAccommodationReferralOutcomeNoteNotApplicableException()
    }
  }

  fun snapshot() = OtherAccommodationReferralSnapshot(
    id = id,
    caseId = caseId,
    crn = crn,
    referenceNumber = referenceNumber,
    submissionDate = submissionDate!!,
    status = status!!,
    organisationName = organisationName,
    website = website,
    submissionNote = submissionNote,
    outcomeReason = outcomeReason,
    outcomeNote = outcomeNote,
    notes = notes.toList(),
  )

  data class OtherAccommodationReferralSnapshot(
    val id: UUID,
    val caseId: UUID,
    val crn: String,
    val referenceNumber: String?,
    val submissionDate: LocalDate,
    val status: OtherAccommodationReferralStatus,
    val organisationName: String? = null,
    val website: String? = null,
    val submissionNote: String? = null,
    val outcomeReason: OtherAccommodationReferralOutcomeReason? = null,
    val outcomeNote: String? = null,
    val notes: List<OtherAccommodationReferralNote> = emptyList(),
  )

  data class OtherAccommodationReferralNote(
    val id: UUID,
    val note: String,
  )
}
