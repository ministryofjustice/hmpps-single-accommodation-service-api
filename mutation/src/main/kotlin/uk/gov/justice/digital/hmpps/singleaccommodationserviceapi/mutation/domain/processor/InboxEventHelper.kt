package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent

@Service
class InboxEventHelper(private val jsonMapper: JsonMapper) {
  private val log = LoggerFactory.getLogger(javaClass)
  fun toDomainEvent(inboxEvent: InboxEventHandler.InboxEvent): SnsDomainEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)

  fun findCrn(inboxEvent: InboxEventHandler.InboxEvent) = findPersonReference(inboxEvent, "CRN")

  fun findPrisonNumber(inboxEvent: InboxEventHandler.InboxEvent) = findPersonReference(inboxEvent, "NOMS", "Prison Number")

  fun findPersonReference(inboxEvent: InboxEventHandler.InboxEvent, name: String, label: String = name): String {
    val value =
      requireNotNull(toDomainEvent(inboxEvent).personReference[name]) { "$label not found in [inboxEventId=${inboxEvent.id}]" }
    log.debug("Found [$name={}] in [inboxEventId={}] ", value, inboxEvent.id)
    return value
  }
}

fun SnsDomainEvent.getRequiredAdditionalInformation(field: String): String = requireNotNull(additionalInformation?.get(field)?.toString()) {
  "Additional information missing for [field=$field]"
}

fun SnsDomainEvent.getAdditionalInformation(field: String): String? = additionalInformation?.get(field)?.toString()
