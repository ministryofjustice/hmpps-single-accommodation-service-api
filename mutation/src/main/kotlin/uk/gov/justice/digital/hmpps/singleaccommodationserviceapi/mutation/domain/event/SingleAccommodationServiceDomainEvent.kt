package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.event

import java.util.UUID

interface AccommodationDataDomainEvent {
  val aggregateId: UUID
  val type: AccommodationDataDomainEventType
}

enum class AccommodationDataDomainEventType {
  PROPOSED_ACCOMMODATION_CREATED;

  companion object {
    fun from(eventType: String): AccommodationDataDomainEventType? = entries.find { it.name == eventType }
  }
}
