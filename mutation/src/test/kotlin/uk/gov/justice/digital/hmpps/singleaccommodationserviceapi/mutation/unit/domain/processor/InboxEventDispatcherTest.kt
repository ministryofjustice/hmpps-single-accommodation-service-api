package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPendingInboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.InboxEventService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.DispatcherConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventDispatcher
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler

/**
 * Concurrency and batching is tested by [InboxEventDispatcherIT]
 */
@ExtendWith(MockKExtension::class)
class InboxEventDispatcherTest {

  @RelaxedMockK
  lateinit var inboxEventRepository: InboxEventRepository

  @RelaxedMockK
  lateinit var inboxEventService: InboxEventService

  @Test
  fun `no events, do nothing`() {
    mockFindAllPending(emptyList())

    val stats = inboxEventDispatcher().process()

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.notProcessedCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(0)
    assertThat(stats.failedCount).isEqualTo(0)

    verify { inboxEventService wasNot Called }
  }

  @Test
  fun `single event, no handler, skip`() {
    mockFindAllPending(listOf(buildPendingInboxEventEntity()))

    val stats = inboxEventDispatcher(
      handlers = emptyList(),
    ).process()

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.notProcessedCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(1)
    assertThat(stats.failedCount).isEqualTo(0)

    verify { inboxEventService wasNot Called }
  }

  @Test
  fun `single event, can't resolve event type, skip`() {
    val event = buildPendingInboxEventEntity(
      eventType = "non.existent.eventType",
    )

    mockFindAllPending(listOf(event))

    val stats = inboxEventDispatcher(
      handlers = emptyList(),
    ).process()

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.notProcessedCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(1)
    assertThat(stats.failedCount).isEqualTo(0)

    verify { inboxEventService wasNot Called }
  }

  @Test
  fun `handler returns PROCESSED`() {
    val event = buildPendingInboxEventEntity(
      eventType = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE.typeName,
    )

    mockFindAllPending(listOf(event))

    val handler = MockEventHandler(
      supportedEventType = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE,
      result = InboxEventHandler.Result.PROCESSED,
    )

    val stats = inboxEventDispatcher(
      handlers = listOf(handler),
    ).process()

    assertThat(handler.processedEvents).containsExactly(event)

    assertThat(stats.processedCount).isEqualTo(1)
    assertThat(stats.notProcessedCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(0)
    assertThat(stats.failedCount).isEqualTo(0)

    verify { inboxEventService.updateInboxEventStatusAndSave(event, ProcessedStatus.PROCESSED) }
  }

  @Test
  fun `handler returns NOT PROCESSED`() {
    val event = buildPendingInboxEventEntity(
      eventType = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE.typeName,
    )

    mockFindAllPending(listOf(event))

    val handler = MockEventHandler(
      supportedEventType = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE,
      result = InboxEventHandler.Result.NOT_PROCESSED,
    )

    val stats = inboxEventDispatcher(
      handlers = listOf(handler),
    ).process()

    assertThat(handler.processedEvents).containsExactly(event)

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.notProcessedCount).isEqualTo(1)
    assertThat(stats.skippedCount).isEqualTo(0)
    assertThat(stats.failedCount).isEqualTo(0)

    verify { inboxEventService.updateInboxEventStatusAndSave(event, ProcessedStatus.NOT_PROCESSED) }
  }

  @Test
  fun `handler returns FAILED`() {
    val event = buildPendingInboxEventEntity(
      eventType = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE.typeName,
    )

    mockFindAllPending(listOf(event))

    val handler = MockEventHandler(
      supportedEventType = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE,
      result = InboxEventHandler.Result.FAILED,
    )

    val stats = inboxEventDispatcher(
      handlers = listOf(handler),
    ).process()

    assertThat(handler.processedEvents).containsExactly(event)

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.notProcessedCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(0)
    assertThat(stats.failedCount).isEqualTo(1)

    verify { inboxEventService.updateInboxEventStatusAndSave(event, ProcessedStatus.FAILED) }
  }

  @Test
  fun `handler throws Exception, failed`() {
    val event = buildPendingInboxEventEntity(
      eventType = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE.typeName,
    )

    mockFindAllPending(listOf(event))

    val handler = MockEventHandler(
      supportedEventType = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE,
      responseException = Exception("error message"),
      result = InboxEventHandler.Result.FAILED,
    )

    val stats = inboxEventDispatcher(
      handlers = listOf(handler),
    ).process()

    assertThat(handler.processedEvents).containsExactly(event)

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.notProcessedCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(0)
    assertThat(stats.failedCount).isEqualTo(1)

    verify { inboxEventService.updateInboxEventStatusAndSave(event, ProcessedStatus.FAILED) }
  }

  private fun inboxEventDispatcher(
    handlers: List<InboxEventHandler> = emptyList(),
  ) = InboxEventDispatcher(
    inboxEventRepository = inboxEventRepository,
    handlers = handlers,
    dispatcherConfig = DispatcherConfig(
      maxEventsPerBatch = 10,
      maxConcurrentEvents = 4,
    ),
    inboxEventService = inboxEventService,
  )

  private data class MockEventHandler(
    val supportedEventType: IncomingHmppsDomainEventType,
    val result: InboxEventHandler.Result,
    val responseException: Throwable? = null,
    val processedEvents: MutableList<InboxEventEntity> = mutableListOf(),
  ) : InboxEventHandler {
    override fun supportedEventType() = supportedEventType
    override fun handle(inboxEvent: InboxEventEntity): InboxEventHandler.Result {
      processedEvents.add(inboxEvent)

      if (responseException != null) {
        throw responseException
      }

      return result
    }
  }

  private fun mockFindAllPending(pending: List<InboxEventEntity>) {
    val pageable = PageRequest.of(
      0,
      10,
      Sort.by("eventOccurredAt").ascending(),
    )

    every { inboxEventRepository.findAllByProcessedStatus(ProcessedStatus.PENDING, pageable) } returns pending
  }
}
