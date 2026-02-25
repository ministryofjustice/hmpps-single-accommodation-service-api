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
  fun process() = runBlocking {
    val inboxEvents = inboxEventRepository.findAllByProcessedStatus(ProcessedStatus.PENDING, pageable)
    if (inboxEvents.isEmpty()) {
      log.debug("No pending inbox events to process")
      return@runBlocking
    }

    log.info("Processing inbox batch [count={}, eventIds={}]", inboxEvents.size, inboxEvents.map { it.id })

    val successCount = AtomicInteger(0)
    val failureCount = AtomicInteger(0)
    val skippedCount = AtomicInteger(0)

    val (partitions, noHandlerEvents) = partitionByKey(inboxEvents)
    noHandlerEvents.forEach {
      log.error("No handler registered for event type [inboxEventId={}, eventType={}]", it.id, it.eventType)
      skippedCount.incrementAndGet()
    }
    log.debug("Partitioned into {} groups", partitions.size)

    coroutineScope {
      partitions.map { (_, events) ->
        async(Dispatchers.IO) {
          concurrencyLimit.withPermit {
            events.forEach { dispatchEvent(it, successCount, failureCount, skippedCount) }
          }
        }
      }.awaitAll()
    }

    log.info(
      "Inbox batch complete [total={}, success={}, failed={}, skipped={}]",
      inboxEvents.size,
      successCount.get(),
      failureCount.get(),
      skippedCount.get(),
    )
  }

  /**
   * Groups events by partition key. Events with the same key are processed sequentially. Order
   * within each partition preserves eventOccurredAt.
   *
   * Returns partitions and events with no registered handler.
   */
  private fun partitionByKey(
    inboxEvents: List<InboxEventEntity>,
  ): PartitionByKey {
    val (withHandler, noHandler) =
      inboxEvents.partition { event ->
        IncomingHmppsDomainEventType.from(event.eventType)?.let { handlerMap[it] } != null
      }
    val partitions =
      withHandler.groupBy { event ->
        val handler =
          IncomingHmppsDomainEventType.from(event.eventType)!!.let { handlerMap[it] }!!
        handler.getPartitionKey(event) ?: event.id.toString()
      }
    return PartitionByKey(partitions, noHandler)
  }

  private fun dispatchEvent(
    inboxEvent: InboxEventEntity,
    successCount: AtomicInteger,
    failureCount: AtomicInteger,
    skippedCount: AtomicInteger,
  ) {
    val handler =
      IncomingHmppsDomainEventType.from(inboxEvent.eventType)?.let { handlerMap[it] }
        ?: run {
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
      log.error("Unexpected error dispatching to handler [inboxEventId={}, eventType={}, error={}]", inboxEvent.id, inboxEvent.eventType, e.message)
      log.debug("Dispatch failure details", e)
      failureCount.incrementAndGet()
    }
  }

  data class PartitionByKey(
    val partitions: Map<String, List<InboxEventEntity>>,
    val noHandlers: List<InboxEventEntity>,
  )
}
