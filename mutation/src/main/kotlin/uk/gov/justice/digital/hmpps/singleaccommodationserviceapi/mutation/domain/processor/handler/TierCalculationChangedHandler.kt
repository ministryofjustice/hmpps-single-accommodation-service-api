package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler

@Component
@ConfigurationProperties(prefix = "tier")
data class TierEventHandlerConfig(
  var v3Enabled: Boolean = false,
)

@Component
class TierCalculationChangedHandler(
  private val jsonMapper: JsonMapper,
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestService: CaseRefreshRequestService?,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)
  override fun supportedEventTypes() = setOf(IncomingHmppsDomainEventType.TIER_CALCULATION_CHANGED.typeName)

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String? {
    val tierDomainEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    return tierDomainEvent.personReference.findCrn()
  }

  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    log.info("Processing tier calculation event [inboxEventId={}]", inboxEvent.id)

    val crn = requireNotNull(getPartitionKey(inboxEvent)) {
      "CRN not found in event payload [inboxEventId=${inboxEvent.id}]"
    }

    val case = caseRepository.findByCrn(crn) ?: return InboxEventHandler.Result.IGNORED

    caseRefreshRequestService?.requestLiveRefresh(case.id)
    log.info("Tier event processed successfully [inboxEventId={}, crn={}]", inboxEvent.id, crn)
    return InboxEventHandler.Result.PROCESSED
  }
}
