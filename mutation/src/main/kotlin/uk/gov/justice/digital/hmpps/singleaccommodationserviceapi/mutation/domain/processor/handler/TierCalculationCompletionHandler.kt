package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.TierDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.uri
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import java.time.Instant

@Component
class TierCalculationCompletionHandler(
  private val inboxEventRepository: InboxEventRepository,
  private val caseApplicationService: CaseApplicationService,
  private val jsonMapper: JsonMapper,
  private val tierClient: TierClient,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventType() = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE

  override fun getPartitionKey(inboxEvent: InboxEventEntity): String? {
    val tierDomainEvent = jsonMapper.readValue(inboxEvent.payload, TierDomainEvent::class.java)
    return tierDomainEvent.personReference.findCrn()
  }

  @Transactional
  override fun handle(inboxEvent: InboxEventEntity) {
    log.info("Processing tier calculation event [inboxEventId={}]", inboxEvent.id)
    log.debug("Tier callback URL [detailUrl={}]", inboxEvent.eventDetailUrl)

    try {
      val newTier = tierClient.getTier(uri = inboxEvent.uri())
      log.info(
        "Tier fetched successfully [inboxEventId={}, tierScore={}]",
        inboxEvent.id,
        newTier.tierScore,
      )
      log.debug("Tier response [inboxEventId={}, tier={}]", inboxEvent.id, newTier)

      val tierDomainEvent = jsonMapper.readValue(inboxEvent.payload, TierDomainEvent::class.java)

      val crn = checkNotNull(tierDomainEvent.personReference.findCrn()) {
        "CRN not found in event payload [inboxEventId=${inboxEvent.id}]"
      }

      log.debug("Upserting case [inboxEventId={}, crn={}]", inboxEvent.id, crn)
      caseApplicationService.upsertTier(tier = newTier, crn = crn)

      inboxEvent.processedStatus = ProcessedStatus.SUCCESS
      inboxEvent.processedAt = Instant.now()
      inboxEventRepository.save(inboxEvent)
      log.info("Tier event processed successfully [inboxEventId={}, crn={}]", inboxEvent.id, crn)
    } catch (e: Exception) {
      log.error(
        "Failed to process tier event [inboxEventId={}, error={}]",
        inboxEvent.id,
        e.message,
      )
      log.debug("Tier processing failure details", e)
      inboxEvent.processedStatus = ProcessedStatus.FAILED
      inboxEvent.processedAt = Instant.now()
      inboxEventRepository.save(inboxEvent)
    }
  }
}
