package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger

/**
 * Dispatches inbox events to registered handlers. Each event is processed in isolation - handlers
 * manage their own transactions. Add new event types by implementing [InboxEventHandler] and
 * registering as a Spring bean.
 *
 * Uses virtual threads for parallel processing with a semaphore to limit concurrency. Events are
 * fetched ordered by [eventOccurredAt] ascending.
 */
@Profile(value = ["local", "dev", "test"])
@Component
class InboxEventDispatcher(
  private val inboxEventRepository: InboxEventRepository,
  handlers: List<InboxEventHandler>,
  @Value($$"${hmpps.sqs.dispatcher.max-events-per-batch:10}") maxEventsPerBatch: Int,
  @Value($$"${hmpps.sqs.dispatcher.max-concurrent-events:4}") maxConcurrentEvents: Int,
  ) {
  private val log = LoggerFactory.getLogger(javaClass)

  private val handlerMap: Map<IncomingHmppsDomainEventType, InboxEventHandler> =
    handlers.associateBy { it.supportedEventType() }

  private val executor = Executors.newVirtualThreadPerTaskExecutor()
  private val concurrencyLimit = Semaphore(maxConcurrentEvents)

  private val pageable = PageRequest.of(
    0,
    maxEventsPerBatch,
    Sort.by("eventOccurredAt").ascending(),
  )

  @Scheduled(fixedDelay = 5000)
  @SchedulerLock(
    name = "InboxDispatcherProcessor",
    lockAtMostFor = "PT2M",
    lockAtLeastFor = "PT1S",
  )
  fun process() {
    val inboxEvents = inboxEventRepository.findAllByProcessedStatus(ProcessedStatus.PENDING, pageable)
    if (inboxEvents.isEmpty()) {
      log.debug("No pending inbox events to process")
      return
    }

    log.info(
      "Processing inbox batch [count={}, eventIds={}]",
      inboxEvents.size,
      inboxEvents.map { it.id }
    )

    val successCount = AtomicInteger(0)
    val failureCount = AtomicInteger(0)
    val skippedCount = AtomicInteger(0)

    val futures =
      inboxEvents.map { event ->
        executor.submit { dispatchEvent(event, successCount, failureCount, skippedCount) }
      }

    futures.forEach { it.get() }

    log.info(
      "Inbox batch complete [total={}, success={}, failed={}, skipped={}]",
      inboxEvents.size,
      successCount.get(),
      failureCount.get(),
      skippedCount.get(),
    )
  }

  private fun dispatchEvent(
    inboxEvent: InboxEventEntity,
    successCount: AtomicInteger,
    failureCount: AtomicInteger,
    skippedCount: AtomicInteger,
  ) {
    concurrencyLimit.acquire()
    try {
      val handler = IncomingHmppsDomainEventType.from(inboxEvent.eventType)?.let { handlerMap[it] }

      if (handler == null) {
        log.error(
          "No handler registered for event type [inboxEventId={}, eventType={}]",
          inboxEvent.id,
          inboxEvent.eventType,
        )
        log.debug("Registered handlers support: {}", handlerMap.keys.map { it.typeName })
        skippedCount.incrementAndGet()
        return
      }

      try {
        handler.handle(inboxEvent)
        when (inboxEvent.processedStatus) {
          ProcessedStatus.SUCCESS -> successCount.incrementAndGet()
          ProcessedStatus.FAILED -> failureCount.incrementAndGet()
          else -> skippedCount.incrementAndGet()
        }
      } catch (e: Exception) {
        log.error(
          "Unexpected error dispatching to handler [inboxEventId={}, eventType={}, error={}]",
          inboxEvent.id,
          inboxEvent.eventType,
          e.message,
        )
        log.debug("Dispatch failure details", e)
        failureCount.incrementAndGet()
      }
    } finally {
      concurrencyLimit.release()
    }
  }
}