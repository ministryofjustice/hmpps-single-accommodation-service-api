package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.uri
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler

@Component
class TierCalculationCompletionHandler(
  private val caseApplicationService: CaseApplicationService,
  private val jsonMapper: JsonMapper,
  private val tierClient: TierClient,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventType() = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE.typeName

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String? {
    val tierDomainEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    return tierDomainEvent.personReference.findCrn()
  }

  @Transactional
  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    log.info("Processing tier calculation event [inboxEventId={}]", inboxEvent.id)
    log.debug("Tier callback URL [detailUrl={}]", inboxEvent.eventDetailUrl)

    val tier = tierClient.getTier(uri = inboxEvent.uri())
    log.info("Tier fetched successfully [inboxEventId={}, tierScore={}]", inboxEvent.id, tier.tierScore)
    log.debug("Tier response [inboxEventId={}, tier={}]", inboxEvent.id, tier)

    val crn = checkNotNull(getPartitionKey(inboxEvent)) {
      "CRN not found in event payload [inboxEventId=${inboxEvent.id}]"
    }

    log.debug("Updating case [inboxEventId={}, crn={}]", inboxEvent.id, crn)
    caseApplicationService.updateTier(tier = tier, crn = crn)
    log.info("Tier event processed successfully [inboxEventId={}, crn={}]", inboxEvent.id, crn)

    return InboxEventHandler.Result.PROCESSED
  }
}
