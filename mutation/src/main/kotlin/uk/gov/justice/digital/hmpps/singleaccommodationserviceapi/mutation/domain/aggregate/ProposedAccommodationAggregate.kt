package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AddressUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.ArrangementSubTypeDescriptionUnexpectedException
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
  private var status: AccommodationStatus? = null,
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
  }

  fun createProposedAccommodation(
    newName: String?,
    newArrangementType: AccommodationArrangementType,
    newArrangementSubType: AccommodationArrangementSubType?,
    newArrangementSubTypeDescription: String?,
    newSettledType: AccommodationSettledType,
    newStatus: AccommodationStatus,
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
    status = newStatus
    offenderReleaseType = newOffenderReleaseType
    address = newAddress
    startDate = newStartDate
    endDate = newEndDate
    lastUpdatedAt = Instant.now()

    validateProposedAccommodation()

    if (status == AccommodationStatus.PASSED) {
      domainEvents += AddressUpdatedDomainEvent(id)
    }
  }

  private fun validateProposedAccommodation() {
    if (arrangementSubType == AccommodationArrangementSubType.OTHER && arrangementSubTypeDescription.isNullOrEmpty()) {
      throw ArrangementSubTypeDescriptionUnexpectedException()
    } else if (arrangementSubType != AccommodationArrangementSubType.OTHER && !arrangementSubTypeDescription.isNullOrEmpty()) {
      throw ArrangementSubTypeDescriptionUnexpectedException()
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
    status!!,
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
    val status: AccommodationStatus,
    val offenderReleaseType: OffenderReleaseType?,
    val address: AccommodationAddressDetails,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val createdAt: Instant,
    val lastUpdatedAt: Instant?,
  )
}
