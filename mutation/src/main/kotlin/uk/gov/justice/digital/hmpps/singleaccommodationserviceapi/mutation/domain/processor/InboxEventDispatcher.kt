package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.TierDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.uri
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import java.time.Instant
/**
 * Dispatches inbox events to registered handlers. Each event is processed in isolation - handlers
 * manage their own transactions. Add new event types by implementing [InboxEventHandler] and
 * registering as a Spring bean.
 */
@Profile(value = ["local", "dev", "test"])
@Component
class InboxEventDispatcher(
  private val inboxEventRepository: InboxEventRepository,
  handlers: List<InboxEventHandler>,
) {

  private val log = LoggerFactory.getLogger(javaClass)

  private val handlerMap: Map<IncomingHmppsDomainEventType, InboxEventHandler> =
    handlers.associateBy { it.supportedEventType() }

  @Scheduled(fixedDelay = 5000)
  @SchedulerLock(
    name = "InboxDispatcherProcessor",
    lockAtMostFor = "PT2M",
    lockAtLeastFor = "PT1S",
  )
  fun process() {
    val inboxEvents = inboxEventRepository.findAllByProcessedStatus(ProcessedStatus.PENDING)
    if (inboxEvents.isEmpty()) {
      log.debug("No pending inbox events to process")
      return
    }

    log.info(
      "Processing inbox batch [count={}, eventIds={}]",
      inboxEvents.size,
      inboxEvents.map { it.id }
    )

    var successCount = 0
    var failureCount = 0
    var skippedCount = 0

    inboxEvents.forEach { inboxEvent ->
      val eventType = IncomingHmppsDomainEventType.from(inboxEvent.eventType)
      val handler = eventType?.let { handlerMap[it] }

      if (handler == null) {
        log.error(
          "No handler registered for event type [inboxEventId={}, eventType={}]",
          inboxEvent.id,
          inboxEvent.eventType,
        )
        log.debug("Registered handlers support: {}", handlerMap.keys.map { it.typeName })
        skippedCount++
        return@forEach
      }

      try {
        handler.handle(inboxEvent)
        when (inboxEvent.processedStatus) {
          ProcessedStatus.SUCCESS -> successCount++
          ProcessedStatus.FAILED -> failureCount++
          else -> skippedCount++
        }
      } catch (e: Exception) {
        log.error(
          "Unexpected error dispatching to handler [inboxEventId={}, eventType={}, error={}]",
          inboxEvent.id,
          inboxEvent.eventType,
          e.message,
        )
        log.debug("Dispatch failure details", e)
        failureCount++
      }
    }

    log.info(
      "Inbox batch complete [total={}, success={}, failed={}, skipped={}]",
      inboxEvents.size,
      successCount,
      failureCount,
      skippedCount,
    )
  }
}