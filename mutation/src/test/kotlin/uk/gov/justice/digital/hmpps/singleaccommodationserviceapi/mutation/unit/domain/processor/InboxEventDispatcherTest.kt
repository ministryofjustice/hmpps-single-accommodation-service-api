package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPendingInboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.InboxEventService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.DispatcherConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventDispatcher
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventDispatcher.InboxEventDispatcherFailureException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler

/**
 * Concurrency and batching is tested by [InboxEventDispatcherIT]
 */
@ExtendWith(MockKExtension::class)
class InboxEventDispatcherTest {

  @RelaxedMockK
  lateinit var inboxEventService: InboxEventService

  @RelaxedMockK
  lateinit var sentryService: SentryService

  @Test
  fun `no events, do nothing`() {
    every { inboxEventService.findPendingOldestFirst(10) } returns emptyList()

    val stats = inboxEventDispatcher().process()

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.notProcessedCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(0)
    assertThat(stats.failedCount).isEqualTo(0)

    verifyNoEventUpdatesMade()
  }

  @Test
  fun `single event, no handler, skip`() {
    every { inboxEventService.findPendingOldestFirst(10) } returns listOf(buildPendingInboxEventEntity())

    val stats = inboxEventDispatcher(
      handlers = emptyList(),
    ).process()

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.notProcessedCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(1)
    assertThat(stats.failedCount).isEqualTo(0)

    verifyNoEventUpdatesMade()
  }

  @Test
  fun `single event, can't resolve event type, skip`() {
    val event = buildPendingInboxEventEntity(
      eventType = "non.existent.eventType",
    )

    every { inboxEventService.findPendingOldestFirst(10) } returns listOf(event)

    val stats = inboxEventDispatcher(
      handlers = emptyList(),
    ).process()

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.notProcessedCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(1)
    assertThat(stats.failedCount).isEqualTo(0)

    verifyNoEventUpdatesMade()
  }

  @Test
  fun `handler returns PROCESSED`() {
    val event = buildPendingInboxEventEntity(
      eventType = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE.typeName,
    )

    every { inboxEventService.findPendingOldestFirst(10) } returns listOf(event)

    val handler = MockEventHandler(
      supportedEventType = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE,
      result = InboxEventHandler.Result.PROCESSED,
    )

    val stats = inboxEventDispatcher(
      handlers = listOf(handler),
    ).process()

    handler.assertThatHasProcessedEvent(event)

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

    every { inboxEventService.findPendingOldestFirst(10) } returns listOf(event)

    val handler = MockEventHandler(
      supportedEventType = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE,
      result = InboxEventHandler.Result.NOT_PROCESSED,
    )

    val stats = inboxEventDispatcher(
      handlers = listOf(handler),
    ).process()

    handler.assertThatHasProcessedEvent(event)

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

    every { inboxEventService.findPendingOldestFirst(10) } returns listOf(event)

    val handler = MockEventHandler(
      supportedEventType = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE,
      result = InboxEventHandler.Result.FAILED,
    )

    val stats = inboxEventDispatcher(
      handlers = listOf(handler),
    ).process()

    handler.assertThatHasProcessedEvent(event)

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.notProcessedCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(0)
    assertThat(stats.failedCount).isEqualTo(1)

    verify { inboxEventService.updateInboxEventStatusAndSave(event, ProcessedStatus.FAILED) }
    verify { sentryService.captureErrorMessage("Unexpected error dispatching to handler [inboxEventId=${event.id}, eventType=${event.eventType}]") }
  }

  @Test
  fun `handler throws Exception, failed`() {
    val event = buildPendingInboxEventEntity(
      eventType = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE.typeName,
    )

    every { inboxEventService.findPendingOldestFirst(10) } returns listOf(event)

    val exception = Exception("error message")

    val handler = MockEventHandler(
      supportedEventType = IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE,
      responseException = exception,
      result = InboxEventHandler.Result.FAILED,
    )

    val stats = inboxEventDispatcher(
      handlers = listOf(handler),
    ).process()

    handler.assertThatHasProcessedEvent(event)

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.notProcessedCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(0)
    assertThat(stats.failedCount).isEqualTo(1)

    verify { inboxEventService.updateInboxEventStatusAndSave(event, ProcessedStatus.FAILED) }

    val raisedExceptionSlot = slot<InboxEventDispatcherFailureException>()
    verify { sentryService.captureException(capture(raisedExceptionSlot)) }

    assertThat(raisedExceptionSlot.captured.message).isEqualTo("Unexpected error dispatching to handler [inboxEventId=${event.id}, eventType=${event.eventType}]")
    assertThat(raisedExceptionSlot.captured.cause).isEqualTo(exception)
  }

  private fun inboxEventDispatcher(
    handlers: List<InboxEventHandler> = emptyList(),
  ) = InboxEventDispatcher(
    handlers = handlers,
    dispatcherConfig = DispatcherConfig(
      maxEventsPerBatch = 10,
      maxConcurrentEvents = 4,
    ),
    inboxEventService = inboxEventService,
    sentryService = sentryService,
  )

  private data class MockEventHandler(
    val supportedEventType: IncomingHmppsDomainEventType,
    val result: InboxEventHandler.Result,
    val responseException: Throwable? = null,
    val processedEvents: MutableList<InboxEventHandler.InboxEvent> = mutableListOf(),
  ) : InboxEventHandler {
    override fun supportedEventType() = supportedEventType
    override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
      processedEvents.add(inboxEvent)

      if (responseException != null) {
        throw responseException
      }

      return result
    }

    fun assertThatHasProcessedEvent(event: InboxEventEntity) {
      assertThat(processedEvents.map { it.id }).contains(event.id)
    }
  }

  private fun verifyNoEventUpdatesMade() {
    verify(exactly = 0) { inboxEventService.updateInboxEventStatusAndSave(any(), any()) }
  }
}
