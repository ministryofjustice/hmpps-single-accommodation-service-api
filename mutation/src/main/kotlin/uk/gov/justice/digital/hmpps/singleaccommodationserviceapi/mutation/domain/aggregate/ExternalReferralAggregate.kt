package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus.ACCEPTED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus.REJECTED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus.SUBMITTED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralWithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.ExternalReferralInvalidStatusTransitionException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsEmptyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsGreaterThanMaxLengthException
import java.time.LocalDate
import java.util.UUID

private const val NOTE_MAX_LENGTH = 4000

class ExternalReferralAggregate private constructor(
  private val id: UUID,
  private val caseId: UUID,
  private val crn: String,
  private var referenceNumber: String? = null,
  private var submissionDate: LocalDate? = null,
  private var status: ExternalReferralStatus? = null,
  private var organisationName: String? = null,
  private var website: String? = null,
  private var submissionNote: String? = null,
  private var email: String? = null,
  private var phoneNumber: String? = null,
  private var withdrawalReason: ExternalReferralWithdrawalReason? = null,
  private var withdrawalNote: String? = null,
  private var outcomeNote: String? = null,
  private var notes: MutableList<ExternalReferralNote> = mutableListOf(),
) {
  companion object {
    fun hydrateNew(caseId: UUID, crn: String) = ExternalReferralAggregate(
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
      status: ExternalReferralStatus,
      organisationName: String?,
      website: String?,
      submissionNote: String?,
      notes: List<ExternalReferralNote>,
      email: String? = null,
      phoneNumber: String? = null,
      withdrawalReason: ExternalReferralWithdrawalReason? = null,
      withdrawalNote: String? = null,
      outcomeNote: String? = null,
    ) = ExternalReferralAggregate(
      id = id,
      caseId = caseId,
      crn = crn,
      referenceNumber = referenceNumber,
      submissionDate = submissionDate,
      status = status,
      organisationName = organisationName,
      website = website,
      submissionNote = submissionNote,
      email = email,
      phoneNumber = phoneNumber,
      withdrawalReason = withdrawalReason,
      withdrawalNote = withdrawalNote,
      outcomeNote = outcomeNote,
      notes = notes.toMutableList(),
    )
  }

  fun addNote(note: String) {
    validateNote(note)
    notes += ExternalReferralNote(
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

  fun updateExternalReferral(
    submissionDate: LocalDate,
    referenceNumber: String?,
    status: ExternalReferralStatus,
    organisationName: String?,
    website: String?,
    submissionNote: String?,
    email: String? = null,
    phoneNumber: String? = null,
    outcomeNote: String? = null,
  ) {

    // Don't allow transition from any status to SUBMITTED, as this is only valid for new referrals
    if (this.status != null && this.status != SUBMITTED && status == SUBMITTED) {
      throw ExternalReferralInvalidStatusTransitionException()
    }

    if (!(status == ACCEPTED || status == REJECTED || status == SUBMITTED)) {
      throw ExternalReferralInvalidStatusTransitionException()
    }

    this.submissionDate = submissionDate
    this.referenceNumber = referenceNumber
    this.status = status
    this.organisationName = organisationName
    this.website = website
    this.submissionNote = submissionNote?.takeUnless { it.isBlank() }
    this.email = email?.takeUnless { it.isBlank() }
    this.phoneNumber = phoneNumber?.takeUnless { it.isBlank() }
    this.outcomeNote = outcomeNote?.takeUnless { it.isBlank() }
  }

  fun snapshot() = ExternalReferralSnapshot(
    id = id,
    caseId = caseId,
    crn = crn,
    referenceNumber = referenceNumber,
    submissionDate = submissionDate!!,
    status = status!!,
    organisationName = organisationName,
    website = website,
    submissionNote = submissionNote,
    email = email,
    phoneNumber = phoneNumber,
    withdrawalReason = withdrawalReason,
    withdrawalNote = withdrawalNote,
    outcomeNote = outcomeNote,
    notes = notes.toList(),
  )

  data class ExternalReferralSnapshot(
    val id: UUID,
    val caseId: UUID,
    val crn: String,
    val referenceNumber: String?,
    val submissionDate: LocalDate,
    val status: ExternalReferralStatus,
    val organisationName: String? = null,
    val website: String? = null,
    val submissionNote: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val withdrawalReason: ExternalReferralWithdrawalReason? = null,
    val withdrawalNote: String? = null,
    val outcomeNote: String? = null,
    val notes: List<ExternalReferralNote> = emptyList(),
  )

  data class ExternalReferralNote(
    val id: UUID,
    val note: String,
  )
}
