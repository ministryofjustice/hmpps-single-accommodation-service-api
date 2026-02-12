package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AddressUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationArrangementSubTypeDescriptionUnexpectedException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationVerificationNotPassedException
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class ProposedAccommodationAggregate private constructor(
  private val id: UUID,
  private val crn: String,
  private val createdAt: Instant = Instant.now(),
  private var name: String? = null,
  private var arrangementType: AccommodationArrangementType? = null,
  private var arrangementSubType: AccommodationArrangementSubType? = null,
  private var arrangementSubTypeDescription: String? = null,
  private var settledType: AccommodationSettledType? = null,
  private var verificationStatus: VerificationStatus? = null,
  private var nextAccommodationStatus: NextAccommodationStatus? = null,
  private var offenderReleaseType: OffenderReleaseType? = null,
  private var address: AccommodationAddressDetails ?= null,
  private var startDate: LocalDate? = null,
  private var endDate: LocalDate? = null,
  private var lastUpdatedAt: Instant? = null,
) {
  private val domainEvents = mutableListOf<SingleAccommodationServiceDomainEvent>()

  companion object {
    fun hydrateNew(crn: String) = ProposedAccommodationAggregate(
      id = UUID.randomUUID(),
      crn = crn,
    )

    fun hydrateExisting(
      id: UUID,
      crn: String,
      createdAt: Instant,
      name: String?,
      arrangementType: AccommodationArrangementType,
      arrangementSubType: AccommodationArrangementSubType?,
      arrangementSubTypeDescription: String?,
      settledType: AccommodationSettledType,
      verificationStatus: VerificationStatus,
      nextAccommodationStatus: NextAccommodationStatus,
      offenderReleaseType: OffenderReleaseType?,
      address: AccommodationAddressDetails,
      startDate: LocalDate?,
      endDate: LocalDate?,
      lastUpdatedAt: Instant?,
    ) = ProposedAccommodationAggregate(
      id = id,
      crn = crn,
      createdAt = createdAt,
      name = name,
      arrangementType = arrangementType,
      arrangementSubType = arrangementSubType,
      arrangementSubTypeDescription = arrangementSubTypeDescription,
      settledType = settledType,
      verificationStatus = verificationStatus,
      nextAccommodationStatus = nextAccommodationStatus,
      offenderReleaseType = offenderReleaseType,
      address = address,
      startDate = startDate,
      endDate = endDate,
      lastUpdatedAt = lastUpdatedAt,
    )
  }

  fun updateProposedAccommodation(
    newName: String?,
    newArrangementType: AccommodationArrangementType,
    newArrangementSubType: AccommodationArrangementSubType?,
    newArrangementSubTypeDescription: String?,
    newSettledType: AccommodationSettledType,
    newVerificationStatus: VerificationStatus,
    newNextAccommodationStatus: NextAccommodationStatus,
    newAddress: AccommodationAddressDetails,
    newOffenderReleaseType: OffenderReleaseType?,
    newStartDate: LocalDate?,
    newEndDate: LocalDate?,
  ) {
    name = newName
    arrangementType = newArrangementType
    arrangementSubType = newArrangementSubType
    arrangementSubTypeDescription = newArrangementSubTypeDescription
    settledType = newSettledType
    verificationStatus = newVerificationStatus
    nextAccommodationStatus = newNextAccommodationStatus
    offenderReleaseType = newOffenderReleaseType
    address = newAddress
    startDate = newStartDate
    endDate = newEndDate
    lastUpdatedAt = Instant.now()

    validateProposedAccommodation()

    if (nextAccommodationStatus == NextAccommodationStatus.YES) {
      domainEvents += AddressUpdatedDomainEvent(id)
    }
  }

  private fun validateProposedAccommodation() {
    validateStatuses()
    validateArrangement()
  }

  private fun validateStatuses() {
    if (nextAccommodationStatus == NextAccommodationStatus.YES && verificationStatus != VerificationStatus.PASSED) {
      throw AccommodationVerificationNotPassedException()
    }
  }

  private fun validateArrangement() {
    if (arrangementSubType == AccommodationArrangementSubType.OTHER && arrangementSubTypeDescription.isNullOrEmpty()) {
      throw AccommodationArrangementSubTypeDescriptionUnexpectedException()
    } else if (arrangementSubType != AccommodationArrangementSubType.OTHER && !arrangementSubTypeDescription.isNullOrEmpty()) {
      throw AccommodationArrangementSubTypeDescriptionUnexpectedException()
    }
  }

  fun pullDomainEvents(): List<SingleAccommodationServiceDomainEvent> = domainEvents.toList().also { domainEvents.clear() }

  fun snapshot() = ProposedAccommodationSnapshot(
    id,
    crn,
    name,
    arrangementType!!,
    arrangementSubType,
    arrangementSubTypeDescription,
    settledType!!,
    verificationStatus!!,
    nextAccommodationStatus!!,
    offenderReleaseType,
    address!!,
    startDate,
    endDate,
    createdAt,
    lastUpdatedAt
  )

  data class ProposedAccommodationSnapshot(
    val id: UUID,
    val crn: String,
    val name: String?,
    val arrangementType: AccommodationArrangementType,
    val arrangementSubType: AccommodationArrangementSubType?,
    val arrangementSubTypeDescription: String?,
    val settledType: AccommodationSettledType,
    val verificationStatus: VerificationStatus,
    val nextAccommodationStatus: NextAccommodationStatus,
    val offenderReleaseType: OffenderReleaseType?,
    val address: AccommodationAddressDetails,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val createdAt: Instant,
    val lastUpdatedAt: Instant?,
  )
}
