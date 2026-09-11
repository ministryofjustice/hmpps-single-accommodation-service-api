package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsEmptyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsGreaterThanMaxLengthException
import java.time.LocalDate
import java.util.UUID

private const val NOTE_MAX_LENGTH = 4000

class OtherAccommodationReferralAggregate private constructor(
  private val id: UUID,
  private val caseId: UUID,
  private val crn: String,
  private var localAuthorityAreaId: UUID? = null,
  private var referenceNumber: String? = null,
  private var submissionDate: LocalDate? = null,
  private var status: OtherAccommodationReferralStatus? = null,
  private var organisationName: String? = null,
  private var website: String? = null,
  private var submissionNote: String? = null,
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
      localAuthorityAreaId: UUID,
      referenceNumber: String?,
      submissionDate: LocalDate,
      status: OtherAccommodationReferralStatus,
      organisationName: String?,
      website: String?,
      submissionNote: String?,
      notes: List<OtherAccommodationReferralNote>,
    ) = OtherAccommodationReferralAggregate(
      id = id,
      caseId = caseId,
      crn = crn,
      localAuthorityAreaId = localAuthorityAreaId,
      referenceNumber = referenceNumber,
      submissionDate = submissionDate,
      status = status,
      organisationName = organisationName,
      website = website,
      submissionNote = submissionNote,
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
    localAuthorityAreaId: UUID,
    submissionDate: LocalDate,
    referenceNumber: String?,
    status: OtherAccommodationReferralStatus,
    organisationName: String?,
    website: String?,
    submissionNote: String?,
  ) {
    this.localAuthorityAreaId = localAuthorityAreaId
    this.submissionDate = submissionDate
    this.referenceNumber = referenceNumber
    this.status = status
    this.organisationName = organisationName
    this.website = website
    this.submissionNote = submissionNote?.takeUnless { it.isBlank() }
  }

  fun snapshot() = OtherAccommodationReferralSnapshot(
    id = id,
    caseId = caseId,
    crn = crn,
    localAuthorityAreaId = localAuthorityAreaId!!,
    referenceNumber = referenceNumber,
    submissionDate = submissionDate!!,
    status = status!!,
    organisationName = organisationName,
    website = website,
    submissionNote = submissionNote,
    notes = notes.toList(),
  )

  data class OtherAccommodationReferralSnapshot(
    val id: UUID,
    val caseId: UUID,
    val crn: String,
    val localAuthorityAreaId: UUID,
    val referenceNumber: String?,
    val submissionDate: LocalDate,
    val status: OtherAccommodationReferralStatus,
    val organisationName: String? = null,
    val website: String? = null,
    val submissionNote: String? = null,
    val notes: List<OtherAccommodationReferralNote> = emptyList(),
  )

  data class OtherAccommodationReferralNote(
    val id: UUID,
    val note: String,
  )
}
