package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event

import java.time.OffsetDateTime

data class HmppsDomainEvent(
  val eventType: String,
  val version: Int? = null,
  val description: String? = null,
  val detailUrl: String? = null,
  val occurredAt: OffsetDateTime,
  val additionalInformation: Map<String, Any> = emptyMap(),
)
