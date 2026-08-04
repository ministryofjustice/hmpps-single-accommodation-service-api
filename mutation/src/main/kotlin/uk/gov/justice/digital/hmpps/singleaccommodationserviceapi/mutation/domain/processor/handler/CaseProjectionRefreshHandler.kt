package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler

@Component
class CaseProjectionRefreshHandler(
  private val caseRefreshRequestService: CaseRefreshRequestService,
  private val jsonMapper: JsonMapper,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventTypes() = setOf(
    IncomingHmppsDomainEventType.TIER_CALCULATION_CHANGED.typeName,
  )

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String? = parse(inboxEvent).personReference.findCrn()

  @Transactional
  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    val domainEvent = parse(inboxEvent)
    log.info("Processing Case projection change event [inboxEventId={}, eventType={}]", inboxEvent.id, domainEvent.eventType)

    val crn = checkNotNull(domainEvent.personReference.findCrn()) {
      "CRN not found in event payload [inboxEventId=${inboxEvent.id}, eventType=${domainEvent.eventType}]"
    }

    return when (caseRefreshRequestService.requestLiveRefresh(crn)) {
      CaseRefreshRequestService.Result.REQUESTED -> {
        log.info("Case projection refresh requested [inboxEventId={}, eventType={}, crn={}]", inboxEvent.id, domainEvent.eventType, crn)
        InboxEventHandler.Result.PROCESSED
      }
      CaseRefreshRequestService.Result.CASE_NOT_FOUND -> {
        log.info("Ignoring projection change event for unknown Case [inboxEventId={}, eventType={}, crn={}]", inboxEvent.id, domainEvent.eventType, crn)
        InboxEventHandler.Result.IGNORED
      }
    }
  }
  private fun parse(inboxEvent: InboxEventHandler.InboxEvent): SnsDomainEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
}
