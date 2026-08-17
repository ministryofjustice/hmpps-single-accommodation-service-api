package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent

@Service
class InboxEventHelper(private val jsonMapper: JsonMapper) {
  private val log = LoggerFactory.getLogger(javaClass)
  fun toDomainEvent(inboxEvent: InboxEventHandler.InboxEvent): SnsDomainEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)

  fun findCrn(inboxEvent: InboxEventHandler.InboxEvent): String {
    val crn =
      requireNotNull(toDomainEvent(inboxEvent).personReference.findCrn()) { "CRN not found in [inboxEventId=${inboxEvent.id}]" }
    log.debug("Found [crn={}] in [inboxEventId={}] ", crn, inboxEvent.id)
    return crn
  }
}

fun SnsDomainEvent.getAdditionalInformation(field: String): String = requireNotNull(additionalInformation?.get(field)?.toString()) {
  "Additional information missing for [field=$field]"
}
