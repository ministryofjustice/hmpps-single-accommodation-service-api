package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsEmptyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsGreaterThanMaxLengthException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.OutOfRegionReferralInvalidStatusException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.OutOfRegionReferralInvalidStatusTransitionException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.OutOfRegionReferralOutcomeNoteNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.OutOfRegionReferralOutcomeReasonNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.OutOfRegionReferralOutcomeReasonRequiredException
import java.time.LocalDate
import java.util.UUID

private const val NOTE_MAX_LENGTH = 4000

private val ACCEPTED_OUTCOME_REASONS = setOf(
  OutcomeReason.PREVENTION_AND_RELIEF_DUTY,
  OutcomeReason.PRIORITY_NEED,
)
private val NOT_ACCEPTED_OUTCOME_REASONS = setOf(
  OutcomeReason.NO_LOCAL_CONNECTION,
  OutcomeReason.INTENTIONALLY_HOMELESS,
  OutcomeReason.REJECTED_FOR_ANOTHER_REASON,
)

class OutOfRegionReferralAggregate private constructor(
  private val id: UUID,
  private val caseId: UUID,
  private var referenceNumber: String? = null,
  private var submissionDate: LocalDate? = null,
  private var status: OorStatus? = null,
  private var outcomeReason: OutcomeReason? = null,
  private var submissionNote: String? = null,
  private var outcomeNote: String? = null,
  private var notes: MutableList<OutOfRegionReferralNote> = mutableListOf(),
) {
  companion object {
    fun hydrateNew(caseId: UUID) = OutOfRegionReferralAggregate(
      id = UUID.randomUUID(),
      caseId = caseId,
    )

    fun hydrateExisting(
      id: UUID,
      caseId: UUID,
      referenceNumber: String?,
      submissionDate: LocalDate,
      status: OorStatus,
      notes: List<OutOfRegionReferralNote>,
      outcomeReason: OutcomeReason? = null,
      submissionNote: String? = null,
      outcomeNote: String? = null,
    ) = OutOfRegionReferralAggregate(
      id = id,
      caseId = caseId,
      referenceNumber = referenceNumber,
      submissionDate = submissionDate,
      status = status,
      outcomeReason = outcomeReason,
      submissionNote = submissionNote,
      outcomeNote = outcomeNote,
      notes = notes.toMutableList(),
    )
  }

  fun updateOutOfRegionReferral(
    submissionDate: LocalDate,
    referenceNumber: String?,
    status: OorStatus,
    outcomeReason: OutcomeReason? = null,
    submissionNote: String? = null,
    outcomeNote: String? = null,
  ) {
    validateStatusTransition(status)
    validateOutcome(status, outcomeReason, outcomeNote)

    this.submissionDate = submissionDate
    this.referenceNumber = referenceNumber
    this.status = status
    this.outcomeReason = outcomeReason

    this.submissionNote = submissionNote?.takeUnless { it.isBlank() }?.also { validateNoteLength(it) }
    if (status == OorStatus.ACCEPTED || status == OorStatus.NOT_ACCEPTED) {
      this.outcomeNote = outcomeNote?.takeUnless { it.isBlank() }?.also { validateNoteLength(it) }
    }
  }

  fun addNote(note: String) {
    validateNote(note)
    notes += OutOfRegionReferralNote(
      id = UUID.randomUUID(),
      note,
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

  private fun validateStatusTransition(newStatus: OorStatus) {
    when (this.status) {
      null -> if (newStatus != OorStatus.SUBMITTED) throw OutOfRegionReferralInvalidStatusException()
      OorStatus.SUBMITTED -> Unit
      OorStatus.ACCEPTED, OorStatus.NOT_ACCEPTED ->
        if (newStatus == OorStatus.SUBMITTED) throw OutOfRegionReferralInvalidStatusTransitionException()
    }
  }

  private fun validateOutcome(newStatus: OorStatus, outcomeReason: OutcomeReason?, outcomeNote: String?) {
    when (newStatus) {
      OorStatus.ACCEPTED -> validateOutcomeReason(outcomeReason, ACCEPTED_OUTCOME_REASONS)
      OorStatus.NOT_ACCEPTED -> validateOutcomeReason(outcomeReason, NOT_ACCEPTED_OUTCOME_REASONS)
      OorStatus.SUBMITTED -> validateNoOutcome(outcomeReason, outcomeNote)
    }
  }

  private fun validateOutcomeReason(
    outcomeReason: OutcomeReason?,
    validReasons: Set<OutcomeReason>,
  ) {
    when {
      outcomeReason == null ->
        throw OutOfRegionReferralOutcomeReasonRequiredException()

      outcomeReason !in validReasons ->
        throw OutOfRegionReferralOutcomeReasonNotApplicableException()
    }
  }

  private fun validateNoOutcome(
    outcomeReason: OutcomeReason?,
    outcomeNote: String?,
  ) {
    if (outcomeReason != null) {
      throw OutOfRegionReferralOutcomeReasonNotApplicableException()
    }

    if (!outcomeNote.isNullOrBlank()) {
      throw OutOfRegionReferralOutcomeNoteNotApplicableException()
    }
  }

  fun snapshot() = OutOfRegionReferralSnapshot(
    id = id,
    caseId = caseId,
    referenceNumber = referenceNumber,
    submissionDate = submissionDate!!,
    status = status!!,
    notes = notes.toList(),
    outcomeReason = outcomeReason,
    submissionNote = submissionNote,
    outcomeNote = outcomeNote,
  )

  data class OutOfRegionReferralSnapshot(
    val id: UUID,
    val caseId: UUID,
    val referenceNumber: String?,
    val submissionDate: LocalDate,
    val status: OorStatus,
    val notes: List<OutOfRegionReferralNote>,
    val outcomeReason: OutcomeReason? = null,
    val submissionNote: String? = null,
    val outcomeNote: String? = null,
  )

  data class OutOfRegionReferralNote(
    val id: UUID,
    val note: String,
  )
}
