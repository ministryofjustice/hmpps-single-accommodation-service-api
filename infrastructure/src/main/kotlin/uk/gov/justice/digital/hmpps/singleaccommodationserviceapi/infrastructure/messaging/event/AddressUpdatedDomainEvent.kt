package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event

import java.util.UUID

data class AddressUpdatedDomainEvent(
  override val aggregateId: UUID,
  override val type: SingleAccommodationServiceDomainEventType = SingleAccommodationServiceDomainEventType.SAS_ADDRESS_UPDATED,
) : SingleAccommodationServiceDomainEvent
