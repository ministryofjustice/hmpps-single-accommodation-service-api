package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.DutyToReferUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferInvalidStatusException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferInvalidStatusTransitionException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferOutcomeNoteNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferOutcomeReasonChangedOnWithdrawal
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferOutcomeReasonNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferOutcomeReasonRequiredException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferWithdrawalReasonNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferWithdrawalReasonOtherGreaterThanMaxLengthException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferWithdrawalReasonRequiredException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsEmptyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsGreaterThanMaxLengthException
import java.time.LocalDate
import java.util.UUID

private const val NOTE_MAX_LENGTH = 4000
private const val WITHDRAWAL_REASON_OTHER_MAX_LENGTH = 4000

private val ACCEPTED_OUTCOME_REASONS = setOf(
  OutcomeReason.PREVENTION_AND_RELIEF_DUTY,
  OutcomeReason.PRIORITY_NEED,
)
private val NOT_ACCEPTED_OUTCOME_REASONS = setOf(
  OutcomeReason.NO_LOCAL_CONNECTION,
  OutcomeReason.INTENTIONALLY_HOMELESS,
  OutcomeReason.REJECTED_FOR_ANOTHER_REASON,
)

class DutyToReferAggregate private constructor(
  private val id: UUID,
  private val caseId: UUID,
  private var localAuthorityAreaId: UUID? = null,
  private var referenceNumber: String? = null,
  private var submissionDate: LocalDate? = null,
  private var status: DtrStatus? = null,
  private var withdrawalReason: WithdrawalReason? = null,
  private var withdrawalReasonOther: String? = null,
  private var outcomeReason: OutcomeReason? = null,
  private var submissionNote: String? = null,
  private var outcomeNote: String? = null,
  private var notes: MutableList<DutyToReferNote> = mutableListOf(),
) {
  private val domainEvents = mutableListOf<SingleAccommodationServiceDomainEvent>()

  companion object {
    fun hydrateNew(caseId: UUID) = DutyToReferAggregate(
      id = UUID.randomUUID(),
      caseId = caseId,
    )

    fun hydrateExisting(
      id: UUID,
      caseId: UUID,
      localAuthorityAreaId: UUID,
      referenceNumber: String?,
      submissionDate: LocalDate,
      status: DtrStatus,
      notes: List<DutyToReferNote>,
      withdrawalReason: WithdrawalReason? = null,
      withdrawalReasonOther: String? = null,
      outcomeReason: OutcomeReason? = null,
      submissionNote: String? = null,
      outcomeNote: String? = null,
    ) = DutyToReferAggregate(
      id = id,
      caseId = caseId,
      localAuthorityAreaId = localAuthorityAreaId,
      referenceNumber = referenceNumber,
      submissionDate = submissionDate,
      status = status,
      withdrawalReason = withdrawalReason,
      withdrawalReasonOther = withdrawalReasonOther,
      outcomeReason = outcomeReason,
      submissionNote = submissionNote,
      outcomeNote = outcomeNote,
      notes = notes.toMutableList(),
    )
  }

  fun updateDutyToRefer(
    localAuthorityAreaId: UUID,
    submissionDate: LocalDate,
    referenceNumber: String?,
    status: DtrStatus,
    withdrawalReason: WithdrawalReason? = null,
    withdrawalReasonOther: String? = null,
    outcomeReason: OutcomeReason? = null,
    submissionNote: String? = null,
    outcomeNote: String? = null,
  ) {
    validateStatusTransition(status)
    validateWithdrawal(status, withdrawalReason, withdrawalReasonOther)

    val previousStatus = this.status
    val previousOutcomeReason = this.outcomeReason

    validateOutcome(status, outcomeReason, outcomeNote, previousStatus, previousOutcomeReason)

    this.localAuthorityAreaId = localAuthorityAreaId
    this.submissionDate = submissionDate
    this.referenceNumber = referenceNumber
    this.status = status
    this.withdrawalReason = withdrawalReason
    this.withdrawalReasonOther = withdrawalReasonOther
    this.outcomeReason = outcomeReason

    // submission note can be changed when updating with any status (user can go back and edit after adding outcome)
    this.submissionNote = submissionNote?.takeUnless { it.isBlank() }?.also { validateNoteLength(it) }
    // outcome note can only change when updating with an outcome status
    if (status == DtrStatus.ACCEPTED || status == DtrStatus.NOT_ACCEPTED) {
      this.outcomeNote = outcomeNote?.takeUnless { it.isBlank() }?.also { validateNoteLength(it) }
    }

    if (previousStatus != status) {
      domainEvents += DutyToReferUpdatedDomainEvent(id)
    }
  }

  fun withdrawDutyToRefer(withdrawalReason: WithdrawalReason) {
    if (this.status == DtrStatus.WITHDRAWN) return
    validateStatusTransition(DtrStatus.WITHDRAWN)
    validateWithdrawal(DtrStatus.WITHDRAWN, withdrawalReason, null)

    this.status = DtrStatus.WITHDRAWN
    this.withdrawalReason = withdrawalReason
    this.withdrawalReasonOther = null
    this.outcomeReason = null

    domainEvents += DutyToReferUpdatedDomainEvent(id)
  }

  fun addNote(note: String) {
    validateNote(note)
    notes += DutyToReferNote(
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

  private fun validateStatusTransition(newStatus: DtrStatus) {
    when (this.status) {
      null -> if (newStatus != DtrStatus.SUBMITTED) throw DutyToReferInvalidStatusException()
      DtrStatus.SUBMITTED -> Unit
      DtrStatus.WITHDRAWN -> if (newStatus != DtrStatus.WITHDRAWN) throw DutyToReferInvalidStatusTransitionException()
      else -> if (newStatus == DtrStatus.SUBMITTED) throw DutyToReferInvalidStatusTransitionException()
    }
  }

  // validate against all withdrawal related fields for incoming change
  private fun validateWithdrawal(newStatus: DtrStatus, reason: WithdrawalReason?, reasonOther: String?) {
    when {
      // if status is not WITHDRAWN
      newStatus != DtrStatus.WITHDRAWN -> {
        // validate no reason or free text reason is provided
        if (reason != null || reasonOther != null) throw DutyToReferWithdrawalReasonNotApplicableException()
      }
      // withdrawal - mandatory reason enum check
      reason == null -> throw DutyToReferWithdrawalReasonRequiredException()
      // withdrawal - free text reason validation
      reason == WithdrawalReason.OTHER -> {
        if (reasonOther.isNullOrBlank()) throw DutyToReferWithdrawalReasonRequiredException()
        if (reasonOther.length > WITHDRAWAL_REASON_OTHER_MAX_LENGTH) throw DutyToReferWithdrawalReasonOtherGreaterThanMaxLengthException()
      }
      // Non-OTHER reason enum provided — validate no free text reason is provided
      reasonOther != null -> throw DutyToReferWithdrawalReasonNotApplicableException()
    }
  }

  // validate the outcome reason and note for incoming change
  private fun validateOutcome(newStatus: DtrStatus, outcomeReason: OutcomeReason?, outcomeNote: String?, previousStatus: DtrStatus?, prevOutcomeReason: OutcomeReason?) {
    when (newStatus) {
      DtrStatus.WITHDRAWN -> when (previousStatus) {
        DtrStatus.ACCEPTED -> validateOutcomeReasonUnchanged(outcomeReason, prevOutcomeReason, ACCEPTED_OUTCOME_REASONS)
        DtrStatus.NOT_ACCEPTED -> validateOutcomeReasonUnchanged(outcomeReason, prevOutcomeReason, NOT_ACCEPTED_OUTCOME_REASONS)
        else -> validateNoOutcome(outcomeReason, outcomeNote)
      }

      DtrStatus.ACCEPTED -> validateOutcomeReason(outcomeReason, ACCEPTED_OUTCOME_REASONS)
      DtrStatus.NOT_ACCEPTED -> validateOutcomeReason(outcomeReason, NOT_ACCEPTED_OUTCOME_REASONS)
      else -> validateNoOutcome(outcomeReason, outcomeNote)
    }
  }

  private fun validateOutcomeReason(
    outcomeReason: OutcomeReason?,
    validReasons: Set<OutcomeReason>,
  ) {
    when {
      outcomeReason == null ->
        throw DutyToReferOutcomeReasonRequiredException()

      outcomeReason !in validReasons ->
        throw DutyToReferOutcomeReasonNotApplicableException()
    }
  }

  private fun validateOutcomeReasonUnchanged(
    outcomeReason: OutcomeReason?,
    previousOutcomeReason: OutcomeReason?,
    validReasons: Set<OutcomeReason>,
  ) {
    when {
      outcomeReason != previousOutcomeReason ->
        throw DutyToReferOutcomeReasonChangedOnWithdrawal()

      outcomeReason !in validReasons ->
        throw DutyToReferOutcomeReasonNotApplicableException()
    }
  }

  private fun validateNoOutcome(
    outcomeReason: OutcomeReason?,
    outcomeNote: String?,
  ) {
    if (outcomeReason != null) {
      throw DutyToReferOutcomeReasonNotApplicableException()
    }

    if (!outcomeNote.isNullOrBlank()) {
      throw DutyToReferOutcomeNoteNotApplicableException()
    }
  }

  fun pullDomainEvents(): List<SingleAccommodationServiceDomainEvent> = domainEvents.toList().also { domainEvents.clear() }

  fun snapshot() = DutyToReferSnapshot(
    id = id,
    caseId = caseId,
    localAuthorityAreaId = localAuthorityAreaId!!,
    referenceNumber = referenceNumber,
    submissionDate = submissionDate!!,
    status = status!!,
    notes = notes.toList(),
    withdrawalReason = withdrawalReason,
    withdrawalReasonOther = withdrawalReasonOther,
    outcomeReason = outcomeReason,
    submissionNote = submissionNote,
    outcomeNote = outcomeNote,
  )

  data class DutyToReferSnapshot(
    val id: UUID,
    val caseId: UUID,
    val localAuthorityAreaId: UUID,
    val referenceNumber: String?,
    val submissionDate: LocalDate,
    val status: DtrStatus,
    val notes: List<DutyToReferNote>,
    val withdrawalReason: WithdrawalReason? = null,
    val withdrawalReasonOther: String? = null,
    val outcomeReason: OutcomeReason? = null,
    val submissionNote: String? = null,
    val outcomeNote: String? = null,
  )

  data class DutyToReferNote(
    val id: UUID,
    val note: String,
  )
}
