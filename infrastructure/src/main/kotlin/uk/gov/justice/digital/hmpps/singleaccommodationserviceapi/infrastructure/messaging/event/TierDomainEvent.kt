package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event

import java.time.OffsetDateTime
import java.util.UUID

data class TierDomainEvent(
  val eventType: String,
  val externalId: UUID,
  val version: Int,
  val description: String? = null,
  val detailUrl: String? = null,
  val occurredAt: OffsetDateTime,
  val personReference: PersonReference = PersonReference(),
)

data class PersonReference(val identifiers: List<PersonIdentifier> = listOf()) {
  fun findCrn() = get("CRN")
  operator fun get(key: String) = identifiers.find { it.type == key }?.value
}

data class PersonIdentifier(val type: String, val value: String)
