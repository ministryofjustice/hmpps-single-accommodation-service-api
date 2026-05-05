package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AccommodationUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationVerificationNotPassedException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsEmptyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsGreaterThanMaxLengthException
import java.time.LocalDate
import java.util.UUID

private const val NOTE_MAX_LENGTH = 4000

class ProposedAccommodationAggregate private constructor(
  private val id: UUID,
  private val caseId: UUID,
  private var name: String? = null,
  private var accommodationType: AccommodationTypeDto? = null,
  private var verificationStatus: VerificationStatus? = null,
  private var nextAccommodationStatus: NextAccommodationStatus? = null,
  private var address: AccommodationAddressDetails? = null,
  private var startDate: LocalDate? = null,
  private var endDate: LocalDate? = null,
  private var notes: MutableList<ProposedAccommodationNoteSnapshot> = mutableListOf(),
) {
  private val domainEvents = mutableListOf<SingleAccommodationServiceDomainEvent>()

  companion object {
    fun hydrateNew(caseId: UUID) = ProposedAccommodationAggregate(
      id = UUID.randomUUID(),
      caseId = caseId,
    )

    fun hydrateExisting(
      id: UUID,
      caseId: UUID,
      name: String?,
      accommodationType: AccommodationTypeDto,
      verificationStatus: VerificationStatus,
      nextAccommodationStatus: NextAccommodationStatus,
      address: AccommodationAddressDetails,
      startDate: LocalDate?,
      endDate: LocalDate?,
      notes: List<ProposedAccommodationNoteSnapshot>,
    ) = ProposedAccommodationAggregate(
      id = id,
      caseId = caseId,
      name = name,
      accommodationType = accommodationType,
      verificationStatus = verificationStatus,
      nextAccommodationStatus = nextAccommodationStatus,
      address = address,
      startDate = startDate,
      endDate = endDate,
      notes = notes.toMutableList(),
    )
  }

  fun updateProposedAccommodation(
    newName: String?,
    newAccommodationType: AccommodationTypeDto,
    newVerificationStatus: VerificationStatus,
    newNextAccommodationStatus: NextAccommodationStatus,
    newAddress: AccommodationAddressDetails,
    newStartDate: LocalDate?,
    newEndDate: LocalDate?,
  ) {
    name = newName
    accommodationType = newAccommodationType
    verificationStatus = newVerificationStatus
    nextAccommodationStatus = newNextAccommodationStatus
    address = newAddress
    startDate = newStartDate
    endDate = newEndDate

    validateProposedAccommodation()

    if (nextAccommodationStatus == NextAccommodationStatus.YES) {
      domainEvents += AccommodationUpdatedDomainEvent(id)
    }
  }

  fun addNote(note: String) {
    validateNote(note)
    notes += ProposedAccommodationNoteSnapshot(
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

  private fun validateProposedAccommodation() {
    validateStatuses()
  }

  private fun validateStatuses() {
    if (nextAccommodationStatus == NextAccommodationStatus.YES && verificationStatus != VerificationStatus.PASSED) {
      throw AccommodationVerificationNotPassedException()
    }
  }

  fun pullDomainEvents(): List<SingleAccommodationServiceDomainEvent> = domainEvents.toList().also { domainEvents.clear() }

  fun snapshot() = ProposedAccommodationSnapshot(
    id,
    caseId,
    name,
    accommodationType!!,
    verificationStatus!!,
    nextAccommodationStatus!!,
    address!!,
    startDate,
    endDate,
    notes = notes.toList(),
  )

  data class ProposedAccommodationSnapshot(
    val id: UUID,
    val caseId: UUID,
    val name: String?,
    val accommodationType: AccommodationTypeDto,
    val verificationStatus: VerificationStatus,
    val nextAccommodationStatus: NextAccommodationStatus,
    val address: AccommodationAddressDetails,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val notes: List<ProposedAccommodationNoteSnapshot>,
  )

  data class ProposedAccommodationNoteSnapshot(
    val id: UUID,
    val note: String,
  )
}
