package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.DutyToReferUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferInvalidStatusException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferInvalidStatusTransitionException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferOutcomeNoteNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferOutcomeReasonNotApplicableException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferOutcomeReasonRequiredException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferSubmissionNoteNotApplicableException
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
    validateOutcome(status, outcomeReason, outcomeNote)
    if (!submissionNote.isNullOrBlank() && status != DtrStatus.SUBMITTED) throw DutyToReferSubmissionNoteNotApplicableException()

    val previousStatus = this.status

    this.localAuthorityAreaId = localAuthorityAreaId
    this.submissionDate = submissionDate
    this.referenceNumber = referenceNumber
    this.status = status
    this.withdrawalReason = withdrawalReason
    this.withdrawalReasonOther = withdrawalReasonOther
    this.outcomeReason = outcomeReason

    if (!submissionNote.isNullOrBlank()) {
      validateNoteLength(submissionNote)
      this.submissionNote = submissionNote
    }
    if (!outcomeNote.isNullOrBlank()) {
      validateNoteLength(outcomeNote)
      this.outcomeNote = outcomeNote
    }

    if (previousStatus != status) {
      domainEvents += DutyToReferUpdatedDomainEvent(id)
    }
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
  private fun validateOutcome(newStatus: DtrStatus, outcomeReason: OutcomeReason?, outcomeNote: String?) {
    when (newStatus) {
      // accepted outcomes must have a valid accepted reason
      DtrStatus.ACCEPTED -> if (outcomeReason !in ACCEPTED_OUTCOME_REASONS) {
        if (outcomeReason == null) throw DutyToReferOutcomeReasonRequiredException()
        throw DutyToReferOutcomeReasonNotApplicableException()
      }
      // not accepted outcomes must have a valid not accepted reason
      DtrStatus.NOT_ACCEPTED -> if (outcomeReason !in NOT_ACCEPTED_OUTCOME_REASONS) {
        if (outcomeReason == null) throw DutyToReferOutcomeReasonRequiredException()
        throw DutyToReferOutcomeReasonNotApplicableException()
      }
      // any other status then an outcome reason or note is not applicable
      else -> {
        if (outcomeReason != null) throw DutyToReferOutcomeReasonNotApplicableException()
        if (!outcomeNote.isNullOrBlank()) throw DutyToReferOutcomeNoteNotApplicableException()
      }
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
