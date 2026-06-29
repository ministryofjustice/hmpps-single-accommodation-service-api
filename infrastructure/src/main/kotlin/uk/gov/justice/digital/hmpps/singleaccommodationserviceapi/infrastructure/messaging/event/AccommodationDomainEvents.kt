package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event

import java.util.UUID

data class AccommodationUpdatedDomainEvent(
  override val aggregateId: UUID,
  val cprAddressId: UUID,
  override val type: SingleAccommodationServiceDomainEventType = SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_UPDATED,
) : SingleAccommodationServiceDomainEvent

data class AccommodationDeletedDomainEvent(
  override val aggregateId: UUID,
  val cprAddressId: UUID,
  override val type: SingleAccommodationServiceDomainEventType = SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_DELETED,
) : SingleAccommodationServiceDomainEvent

data class AccommodationPersonArrivedDomainEvent(
  override val aggregateId: UUID,
  val cprAddressId: UUID,
  override val type: SingleAccommodationServiceDomainEventType = SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_PERSON_ARRIVED,
) : SingleAccommodationServiceDomainEvent
