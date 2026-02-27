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
  SAS_ACCOMMODATION_UPDATED(
    "sas.accommodation.updated",
    "SAS accommodation has been updated",
  ),
  SAS_DUTY_TO_REFER_CREATED(
    "sas.duty-to-refer.created",
    "SAS duty to refer has been created",
  ),
  ;

  companion object {
    fun from(eventType: String): SingleAccommodationServiceDomainEventType? = entries.find { it.name == eventType }
  }
}
