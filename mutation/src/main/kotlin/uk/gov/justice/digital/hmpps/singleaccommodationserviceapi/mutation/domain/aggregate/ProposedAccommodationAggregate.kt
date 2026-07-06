package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AccommodationDeletedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AccommodationPersonArrivedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AccommodationUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationPersonCannotArriveException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationVerificationNotPassedException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsEmptyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsGreaterThanMaxLengthException
import java.time.LocalDate
import java.util.UUID

private const val NOTE_MAX_LENGTH = 4000
private const val PRISON_ACCOMMODATION_TYPE_CODE = "HMP"

enum class SyncType {
  CREATE,
  UPDATE,
}

class ProposedAccommodationAggregate private constructor(
  private val id: UUID,
  private val caseId: UUID,
  private var currentAccommodation: AccommodationSummaryDto?,
  private var accommodationSource: AccommodationSource? = null,
  private var cprAddressId: UUID? = null,
  private var name: String? = null,
  private var accommodationType: AccommodationTypeDto? = null,
  private var accommodationStatus: AccommodationStatusDto? = null,
  private var verificationStatus: VerificationStatus? = null,
  private var nextAccommodationStatus: NextAccommodationStatus? = null,
  private var typeVerified: Boolean? = null,
  private var noFixedAbode: Boolean? = null,
  private var address: AccommodationAddressDetails? = null,
  private var startDate: LocalDate? = null,
  private var endDate: LocalDate? = null,
  private var notes: MutableList<ProposedAccommodationNoteSnapshot> = mutableListOf(),
) {
  private val domainEvents = mutableListOf<SingleAccommodationServiceDomainEvent>()

  companion object {
    fun hydrateNew(
      caseId: UUID,
      cprAddressId: UUID?,
      accommodationSource: AccommodationSource?,
      currentAccommodation: AccommodationSummaryDto?,
    ) = ProposedAccommodationAggregate(
      id = UUID.randomUUID(),
      caseId = caseId,
      cprAddressId = cprAddressId,
      accommodationSource = accommodationSource,
      currentAccommodation = currentAccommodation,
    )

    fun hydrateExisting(
      id: UUID,
      caseId: UUID,
      accommodationSource: AccommodationSource,
      currentAccommodation: AccommodationSummaryDto?,
      cprAddressId: UUID?,
      name: String?,
      accommodationType: AccommodationTypeDto?,
      accommodationStatus: AccommodationStatusDto?,
      verificationStatus: VerificationStatus,
      nextAccommodationStatus: NextAccommodationStatus,
      address: AccommodationAddressDetails,
      startDate: LocalDate?,
      endDate: LocalDate?,
      typeVerified: Boolean?,
      noFixedAbode: Boolean?,
      notes: List<ProposedAccommodationNoteSnapshot>,
    ) = ProposedAccommodationAggregate(
      id = id,
      caseId = caseId,
      accommodationSource = accommodationSource,
      currentAccommodation = currentAccommodation,
      cprAddressId = cprAddressId,
      name = name,
      accommodationType = accommodationType,
      accommodationStatus = accommodationStatus,
      verificationStatus = verificationStatus,
      nextAccommodationStatus = nextAccommodationStatus,
      typeVerified = typeVerified,
      noFixedAbode = noFixedAbode,
      address = address,
      startDate = startDate,
      endDate = endDate,
      notes = notes.toMutableList(),
    )
  }

  fun updateProposedAccommodation(
    newName: String?,
    newAccommodationType: AccommodationTypeDto?,
    newVerificationStatus: VerificationStatus,
    newNextAccommodationStatus: NextAccommodationStatus,
    newAddress: AccommodationAddressDetails,
    newStartDate: LocalDate?,
    newEndDate: LocalDate?,
    newNoFixedAbode: Boolean?,
  ) {
    val previousNextAccommodationStatus = nextAccommodationStatus
    val previousVerificationStatus = verificationStatus
    val existingIsKnownToCpr = isRegisteredWithCpr()
    val shouldPublishUpdateEvent =
      existingIsKnownToCpr &&
        wasNextAccommodation(previousNextAccommodationStatus) &&
        newNextAccommodationStatus == NextAccommodationStatus.YES &&
        hasRelevantCprChanges(
          newAddress,
          newStartDate,
          newEndDate,
          newAccommodationType,
        )
    val shouldPublishDeleteEvent =
      existingIsKnownToCpr &&
        shouldDeleteFromCpr(
          previousNextAccommodationStatus,
          previousVerificationStatus,
          newNextAccommodationStatus,
          newVerificationStatus,
        )

    name = newName
    accommodationType = newAccommodationType
    verificationStatus = newVerificationStatus
    nextAccommodationStatus = newNextAccommodationStatus
    address = newAddress
    startDate = newStartDate
    endDate = newEndDate
    noFixedAbode = newNoFixedAbode

    downgradeNextAccommodationStatusIfVerificationFailed()
    accommodationStatus = getAccommodationStatus()
    typeVerified = false

    validateProposedAccommodation()

    when {
      shouldPublishDeleteEvent -> {
        domainEvents += AccommodationDeletedDomainEvent(
          aggregateId = id,
          cprAddressId = cprAddressId!!,
        )
        unregisterWithCpr()
      }

      shouldPublishUpdateEvent -> {
        domainEvents += AccommodationUpdatedDomainEvent(
          aggregateId = id,
          cprAddressId = cprAddressId!!,
        )
      }
    }
  }

  fun syncProposedAccommodation(
    newAccommodationType: AccommodationTypeDto?,
    newAccommodationStatus: AccommodationStatusDto?,
    newAddress: AccommodationAddressDetails,
    newStartDate: LocalDate?,
    newEndDate: LocalDate?,
    newTypeVerified: Boolean?,
    newNoFixedAbode: Boolean?,
    syncType: SyncType,
  ) {
    name = null
    accommodationType = newAccommodationType
    accommodationStatus = newAccommodationStatus
    typeVerified = newTypeVerified
    noFixedAbode = newNoFixedAbode
    address = newAddress
    startDate = newStartDate
    endDate = newEndDate

    if (SyncType.CREATE == syncType) {
      verificationStatus = VerificationStatus.PASSED
      nextAccommodationStatus = NextAccommodationStatus.YES
    }
  }

  fun arrivePersonAtProposedAccommodation(arrivalDate: LocalDate) {
    if (!canPersonArrive()) {
      throw AccommodationPersonCannotArriveException()
    }
    startDate = arrivalDate
    endDate = null
    accommodationStatus = AccommodationStatusDto(
      code = AddressStatusCode.M.name,
      description = AddressStatusCode.M.description,
    )
    typeVerified = true
    domainEvents += AccommodationPersonArrivedDomainEvent(
      aggregateId = id,
      cprAddressId = cprAddressId!!,
    )
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

  private fun canPersonArrive(): Boolean {
    if (
      isRegisteredWithCpr() &&
      NextAccommodationStatus.YES == nextAccommodationStatus &&
      (AddressStatusCode.PR.name == accommodationStatus?.code || AddressStatusCode.PR1.name == accommodationStatus?.code)
    ) {
      return true
    }
    return false
  }

  fun pullDomainEvents(): List<SingleAccommodationServiceDomainEvent> = domainEvents.toList().also { domainEvents.clear() }

  private fun getAccommodationStatus(): AccommodationStatusDto? = if (nextAccommodationStatus == NextAccommodationStatus.YES) {
    if (PRISON_ACCOMMODATION_TYPE_CODE == currentAccommodation?.type?.code) {
      AccommodationStatusDto(
        code = AddressStatusCode.PR1.name,
        description = AddressStatusCode.PR1.description,
      )
    } else {
      AccommodationStatusDto(
        code = AddressStatusCode.PR.name,
        description = AddressStatusCode.PR.description,
      )
    }
  } else {
    null
  }

  fun requiresCprRegistration() = nextAccommodationStatus == NextAccommodationStatus.YES && !isRegisteredWithCpr()

  private fun isRegisteredWithCpr() = cprAddressId != null

  private fun unregisterWithCpr() {
    cprAddressId = null
  }

  private fun wasNextAccommodation(
    status: NextAccommodationStatus?,
  ) = status == NextAccommodationStatus.YES

  private fun hasRelevantCprChanges(
    newAddress: AccommodationAddressDetails,
    newStartDate: LocalDate?,
    newEndDate: LocalDate?,
    newAccommodationType: AccommodationTypeDto?,
  ) = address != newAddress || startDate != newStartDate || endDate != newEndDate || accommodationType?.code != newAccommodationType?.code

  private fun shouldDeleteFromCpr(
    previousNextAccommodationStatus: NextAccommodationStatus?,
    previousVerificationStatus: VerificationStatus?,
    newNextAccommodationStatus: NextAccommodationStatus,
    newVerificationStatus: VerificationStatus,
  ): Boolean = (
    wasNextAccommodation(previousNextAccommodationStatus) &&
      newNextAccommodationStatus != NextAccommodationStatus.YES
    ) ||
    (
      wasNextAccommodation(previousNextAccommodationStatus) &&
        previousVerificationStatus == VerificationStatus.PASSED &&
        newVerificationStatus == VerificationStatus.FAILED
      )

  private fun downgradeNextAccommodationStatusIfVerificationFailed() {
    if (
      verificationStatus == VerificationStatus.FAILED &&
      nextAccommodationStatus == NextAccommodationStatus.YES
    ) {
      nextAccommodationStatus = NextAccommodationStatus.NO
    }
  }

  fun markRegisteredWithCpr(cprAddressId: UUID) {
    this.cprAddressId = cprAddressId
  }

  fun snapshot() = ProposedAccommodationSnapshot(
    id,
    caseId,
    cprAddressId,
    accommodationSource!!,
    name,
    accommodationType,
    accommodationStatus,
    verificationStatus!!,
    nextAccommodationStatus!!,
    address!!,
    startDate,
    endDate,
    typeVerified,
    noFixedAbode,
    notes = notes.toList(),
  )

  data class ProposedAccommodationSnapshot(
    val id: UUID,
    val caseId: UUID,
    val cprAddressId: UUID?,
    val accommodationSource: AccommodationSource,
    val name: String?,
    val accommodationType: AccommodationTypeDto?,
    val accommodationStatus: AccommodationStatusDto?,
    val verificationStatus: VerificationStatus,
    val nextAccommodationStatus: NextAccommodationStatus,
    val address: AccommodationAddressDetails,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val typeVerified: Boolean?,
    val noFixedAbode: Boolean?,
    val notes: List<ProposedAccommodationNoteSnapshot>,
  )

  data class ProposedAccommodationNoteSnapshot(
    val id: UUID,
    val note: String,
  )
}
