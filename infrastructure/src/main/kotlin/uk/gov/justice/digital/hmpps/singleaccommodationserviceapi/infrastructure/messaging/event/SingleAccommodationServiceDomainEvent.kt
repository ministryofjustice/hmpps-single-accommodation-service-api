package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event

import java.util.UUID

interface SingleAccommodationServiceDomainEvent {
  val aggregateId: UUID
  val type: SingleAccommodationServiceDomainEventType
}

enum class SingleAccommodationServiceDomainEventType(
  val typeName: String,
  val typeDescription: String,
) {
  PROPOSED_ACCOMMODATION_CREATED(
    "adda.proposed.accommodation.created",
    "Proposed accommodation has been created"
  );

  companion object {
    fun from(eventType: String): SingleAccommodationServiceDomainEventType? = entries.find { it.name == eventType }
  }
}
