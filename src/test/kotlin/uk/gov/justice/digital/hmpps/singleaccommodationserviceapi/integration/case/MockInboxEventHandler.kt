package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.springframework.stereotype.Service
import org.springframework.test.context.event.annotation.BeforeTestMethod
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * A mock event handler installed as a spring bean so it can be used when testing
 * the inbox event dispatcher in [InboxEventDispatcherIT]. Partitions on CRN
 *
 * Will always return 'PROCESSED'
 */
@Service
class MockInboxEventHandler(
  private val jsonMapper: JsonMapper,
  val currentCount: AtomicInteger = AtomicInteger(0),
  val maxConcurrent: AtomicInteger = AtomicInteger(0),
) : InboxEventHandler {

  companion object {
    const val EVENT_TYPE: String = "mock.event.handler"
  }

  val processedEvents: MutableList<InboxEventHandler.InboxEvent> = CopyOnWriteArrayList()

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String? {
    val snsDomainEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    return snsDomainEvent.personReference.findCrn()
  }

  override fun supportedEventType() = EVENT_TYPE

  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    val c = currentCount.incrementAndGet()
    maxConcurrent.updateAndGet { maxOf(it, c) }

    try {
      Thread.sleep(150)

      processedEvents.add(inboxEvent)

      return InboxEventHandler.Result.PROCESSED
    } finally {
      currentCount.decrementAndGet()
    }
  }

  fun assertThatHasProcessedEvents(events: List<InboxEventEntity>) {
    assertThat(processedEvents.map { it.id }).containsExactlyInAnyOrderElementsOf(events.map { it.id })
  }

  @BeforeTestMethod
  fun reset() {
    processedEvents.clear()
  }
}
