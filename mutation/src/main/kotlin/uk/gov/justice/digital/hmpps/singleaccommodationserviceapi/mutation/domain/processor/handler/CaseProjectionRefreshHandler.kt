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

  override fun supportedEventType() = IncomingHmppsDomainEventType.TIER_CALCULATION_CHANGED.typeName

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String? {
    val domainEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    return domainEvent.personReference.findCrn()
  }

  @Transactional
  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    log.info("Processing Case projection change event [inboxEventId={}]", inboxEvent.id)

    val crn = checkNotNull(getPartitionKey(inboxEvent)) {
      "CRN not found in event payload [inboxEventId=${inboxEvent.id}]"
    }

    return when (caseRefreshRequestService.requestLiveRefresh(crn)) {
      CaseRefreshRequestService.Result.REQUESTED -> {
        log.info("Case projection refresh requested [inboxEventId={}, crn={}]", inboxEvent.id, crn)
        InboxEventHandler.Result.PROCESSED
      }
      CaseRefreshRequestService.Result.CASE_NOT_FOUND -> {
        log.info("Ignoring projection change event for unknown Case [inboxEventId={}, crn={}]", inboxEvent.id, crn)
        InboxEventHandler.Result.IGNORED
      }
    }
  }
}
