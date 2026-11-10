package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event

import java.util.UUID

data class DutyToReferCreatedDomainEvent(
  override val aggregateId: UUID,
  override val type: SingleAccommodationServiceDomainEventType = SingleAccommodationServiceDomainEventType.SAS_DUTY_TO_REFER_CREATED,
) : SingleAccommodationServiceDomainEvent
