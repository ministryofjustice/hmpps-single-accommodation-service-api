package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.DutyToReferUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferInvalidStatusException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferInvalidStatusTransitionException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferInvalidWithdrawalReasonException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsEmptyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsGreaterThanMaxLengthException
import java.time.LocalDate
import java.util.UUID

private const val NOTE_MAX_LENGTH = 4000

class DutyToReferAggregate private constructor(
  private val id: UUID,
  private val caseId: UUID,
  private var localAuthorityAreaId: UUID? = null,
  private var referenceNumber: String? = null,
  private var submissionDate: LocalDate? = null,
  private var status: DtrStatus? = null,
  private var withdrawalReason: WithdrawalReason? = null,
  private var withdrawalReasonOther: String? = null,
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
    ) = DutyToReferAggregate(
      id = id,
      caseId = caseId,
      localAuthorityAreaId = localAuthorityAreaId,
      referenceNumber = referenceNumber,
      submissionDate = submissionDate,
      status = status,
      withdrawalReason = withdrawalReason,
      withdrawalReasonOther = withdrawalReasonOther,
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
  ) {
    validateStatusTransition(status)
    validateWithdrawal(status, withdrawalReason, withdrawalReasonOther)

    val previousStatus = this.status

    this.localAuthorityAreaId = localAuthorityAreaId
    this.submissionDate = submissionDate
    this.referenceNumber = referenceNumber
    this.status = status
    this.withdrawalReason = withdrawalReason
    this.withdrawalReasonOther = withdrawalReasonOther

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

  private fun validateWithdrawal(newStatus: DtrStatus, reason: WithdrawalReason?, reasonOther: String?) {
    if (newStatus != DtrStatus.WITHDRAWN) {
      if (reason != null || reasonOther != null) throw DutyToReferInvalidWithdrawalReasonException()
      return
    }
    if (reason == null) throw DutyToReferInvalidWithdrawalReasonException()
    if (reason == WithdrawalReason.OTHER) {
      if (reasonOther.isNullOrBlank()) throw DutyToReferInvalidWithdrawalReasonException()
      if (reasonOther.length > NOTE_MAX_LENGTH) throw DutyToReferInvalidWithdrawalReasonException()
    } else {
      if (reasonOther != null) throw DutyToReferInvalidWithdrawalReasonException()
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
  )

  data class DutyToReferNote(
    val id: UUID,
    val note: String,
  )
}
