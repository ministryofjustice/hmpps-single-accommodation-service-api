package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import java.util.concurrent.atomic.AtomicInteger

/**
 * Dispatches inbox events to registered handlers. Each event is processed in isolation - handlers
 * manage their own transactions. Add new event types by implementing [InboxEventHandler] and
 * registering as a Spring bean.
 *
 * Partitions events by [InboxEventHandler.getPartitionKey] so that events for the same key are
 * never processed concurrently. This avoids race conditions when updating the same resource. Events
 * with different keys run in parallel using coroutines. Events are fetched ordered by
 * [eventOccurredAt] ascending; within each partition, this order is preserved.
 */

@Component
@ConfigurationProperties(prefix = "hmpps.sqs.dispatcher")
class DispatcherConfig(
  var maxEventsPerBatch: Int = 10,
  var maxConcurrentEvents: Int = 4,
)

@ConditionalOnProperty(
  name = ["hmpps.sqs.enabled"],
  havingValue = "true",
)
@Component
class InboxEventDispatcher(
  private val inboxEventRepository: InboxEventRepository,
  handlers: List<InboxEventHandler>,
  private val dispatcherConfig: DispatcherConfig,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  private val eventTypeToHandlers: Map<IncomingHmppsDomainEventType, InboxEventHandler> =
    handlers.associateBy { it.supportedEventType() }

  @Scheduled(fixedRateString = $$"${scheduling.fixed-delay}")
  @SchedulerLock(
    name = "InboxDispatcherProcessor",
    lockAtMostFor = $$"${shedlock.inbox-event-dispatcher.lock-at-most-for}",
    lockAtLeastFor = $$"${shedlock.inbox-event-dispatcher.lock-at-least-for}",
  )
  fun process() = runBlocking {
    val pageable = PageRequest.of(
      0,
      dispatcherConfig.maxEventsPerBatch,
      Sort.by("eventOccurredAt").ascending(),
    )

    val inboxEvents = inboxEventRepository.findAllByProcessedStatus(ProcessedStatus.PENDING, pageable)
    if (inboxEvents.isEmpty()) {
      return@runBlocking
    }

    val concurrencyLimit = Semaphore(dispatcherConfig.maxConcurrentEvents)

    log.info("Processing inbox batch [count={}, eventIds={}]", inboxEvents.size, inboxEvents.map { it.id })

    val processedCount = AtomicInteger(0)
    val notProcessedCount = AtomicInteger(0)
    val failedCount = AtomicInteger(0)
    val skippedCount = AtomicInteger(0)

    val (partitions, eventsWithoutHandlers) = partitionByKey(inboxEvents)
    eventsWithoutHandlers.forEach {
      log.error("No handler registered for event type [inboxEventId={}, eventType={}]", it.id, it.eventType)
      skippedCount.incrementAndGet()
    }
    log.debug("Partitioned into {} groups", partitions.size)

    coroutineScope {
      partitions.map { (_, events) ->
        async(Dispatchers.IO) {
          concurrencyLimit.withPermit {
            events.forEach { dispatchEvent(it, processedCount, notProcessedCount, failedCount, skippedCount) }
          }
        }
      }.awaitAll()
    }

    log.info(
      "Inbox batch complete [total={}, processed={}, notProcessed={}, failed={}, skipped={}]",
      inboxEvents.size,
      processedCount.get(),
      notProcessedCount.get(),
      failedCount.get(),
      skippedCount.get(),
    )
  }

  /**
   * Groups events by partition key. The given event order is retained with-in the partition
   * (i.e. by eventOccurredAt asc)
   */
  private fun partitionByKey(
    inboxEvents: List<InboxEventEntity>,
  ): PartitioningResult {
    val (withHandler, withoutHandler) = inboxEvents.partition { it.resolveHandler() != null }
    val partitions: Map<String, List<InboxEventEntity>> = withHandler.groupBy { event ->
      event.resolveHandler()!!.getPartitionKey(event) ?: event.id.toString()
    }
    return PartitioningResult(partitions, withoutHandler)
  }

  private fun dispatchEvent(
    inboxEvent: InboxEventEntity,
    processedCount: AtomicInteger,
    notProcessedCount: AtomicInteger,
    failedCount: AtomicInteger,
    skippedCount: AtomicInteger,
  ) {
    val handler =
      inboxEvent.resolveHandler()
        ?: run {
          log.debug("Registered handlers support: {}", eventTypeToHandlers.keys.map { it.typeName })
          skippedCount.incrementAndGet()
          return
        }

    try {
      handler.handle(inboxEvent)
      when (inboxEvent.processedStatus) {
        ProcessedStatus.PROCESSED -> processedCount.incrementAndGet()
        ProcessedStatus.NOT_PROCESSED -> notProcessedCount.incrementAndGet()
        ProcessedStatus.FAILED -> failedCount.incrementAndGet()
        else -> skippedCount.incrementAndGet()
      }
    } catch (e: Exception) {
      log.error("Unexpected error dispatching to handler [inboxEventId={}, eventType={}, error={}]", inboxEvent.id, inboxEvent.eventType, e.message)
      log.debug("Dispatch failure details", e)
      failedCount.incrementAndGet()
    }
  }

  data class PartitioningResult(
    val partitions: Map<String, List<InboxEventEntity>>,
    val withoutHandlers: List<InboxEventEntity>,
  )

  private fun InboxEventEntity.resolveEventType() = IncomingHmppsDomainEventType.forEventType(eventType)

  private fun InboxEventEntity.resolveHandler() = resolveEventType()?.let { eventTypeToHandlers[it] }
}
