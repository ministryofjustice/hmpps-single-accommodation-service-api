package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event

import java.util.UUID

data class ProposedAccommodationCreatedEvent(
  override val aggregateId: UUID,
  override val type: SingleAccommodationServiceDomainEventType = SingleAccommodationServiceDomainEventType.PROPOSED_ACCOMMODATION_CREATED,
) : SingleAccommodationServiceDomainEvent
