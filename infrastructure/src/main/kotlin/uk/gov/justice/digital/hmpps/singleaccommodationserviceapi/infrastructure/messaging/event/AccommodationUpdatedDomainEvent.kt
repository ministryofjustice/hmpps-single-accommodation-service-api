package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event

import java.util.UUID

data class AccommodationUpdatedDomainEvent(
  override val aggregateId: UUID,
  override val type: SingleAccommodationServiceDomainEventType = SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_UPDATED,
) : SingleAccommodationServiceDomainEvent
