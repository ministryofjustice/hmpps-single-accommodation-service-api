package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE_LIST
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler

@Component
class CaseAllocationHandler(
  private val jsonMapper: JsonMapper,
  private val cacheManager: CacheManager,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventType() = IncomingHmppsDomainEventType.CASE_ALLOCATED.typeName

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String? {
    val caseAllocationEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    return caseAllocationEvent.personReference.findCrn()
  }

  @Transactional
  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    log.info("Processing CaseAllocation event [inboxEventId={}]", inboxEvent.id)

    val crn = checkNotNull(getPartitionKey(inboxEvent)) {
      "CRN not found in event payload [inboxEventId=${inboxEvent.id}]"
    }

    val cache = cacheManager.getCache(GET_CASE_LIST)
    if (cache == null) {
      log.warn("Failed clearing cache. Cache not found: [$GET_CASE_LIST]")
      return InboxEventHandler.Result.FAILED
    } else {
      cache.clear()
      return InboxEventHandler.Result.PROCESSED
      log.info(
        "CaseAllocation event processed successfully [inboxEventId={}, crn={}]. Cache [{}] cleared.",
        inboxEvent.id,
        crn,
        GET_CASE_LIST,
      )
    }
  }
}
