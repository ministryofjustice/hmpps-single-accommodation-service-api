package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event

import java.time.OffsetDateTime
import java.util.UUID

data class PrisonerSearchDomainEvent(
  val eventType: String,
  val externalId: UUID,
  val version: Int,
  val description: String? = null,
  val detailUrl: String? = null,
  val occurredAt: OffsetDateTime,
  val additionalInformation: AdditionalInformation,
  val personReference: PrisonerSearchPersonReference = PrisonerSearchPersonReference(),
)

data class PrisonerSearchPersonReference(val identifiers: List<PrisonerSearchPersonIdentifier> = listOf()) {
  fun findNoms() = get("NOMS")
  operator fun get(key: String) = identifiers.find { it.type == key }?.value
}

data class PrisonerSearchPersonIdentifier(val type: String, val value: String)

data class AdditionalInformation(private val mutableMap: MutableMap<String, Any?> = mutableMapOf()) : MutableMap<String, Any?> by mutableMap

val AdditionalInformation.categoriesChanged get() = (get("categoriesChanged") as List<CategoriesChanged>).toSet()

enum class CategoriesChanged {
  SENTENCE,
}
