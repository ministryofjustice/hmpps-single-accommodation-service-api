package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPendingInboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.InboxEventService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserContextService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.DispatcherConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventDispatcher
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventDispatcher.InboxEventDispatcherFailureException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import java.util.UUID

/**
 * Concurrency and batching is tested by [InboxEventDispatcherIT]
 */
@ExtendWith(MockKExtension::class)
class InboxEventDispatcherTest {

  @RelaxedMockK
  lateinit var inboxEventService: InboxEventService

  @RelaxedMockK
  lateinit var sentryService: SentryService

  @RelaxedMockK
  lateinit var userContextService: UserContextService

  private val sasSystemUser = buildUserEntity(
    id = UUID.randomUUID(),
    username = "SAS_SYSTEM_USER",
    authSource = AuthSource.NONE,
    forename = "SAS",
    middleNames = null,
    surname = "system user",
    email = "SAS_SYSTEM_USER",
  )

  @Test
  fun `no events, do nothing`() {
    every { inboxEventService.findPendingOldestFirst(10) } returns emptyList()

    val stats = inboxEventDispatcher(
      maxEventsPerBatch = 10,
    ).process()

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.ignoredCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(0)
    assertThat(stats.failedCount).isEqualTo(0)

    verify(exactly = 0) { userContextService.setUserContextAsSasSystemUser() }
    verify(exactly = 0) { userContextService.clearContext() }
    verifyNoEventUpdatesMade()
  }

  @Test
  fun `single event, no handler, skip`() {
    val event = buildPendingInboxEventEntity(eventType = "test.event")

    every { inboxEventService.findPendingOldestFirst(10) } returns listOf(event)

    val stats = inboxEventDispatcher(
      handlers = emptyList(),
      maxEventsPerBatch = 10,
    ).process()

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.ignoredCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(1)
    assertThat(stats.failedCount).isEqualTo(0)

    verify(exactly = 0) { userContextService.setUserContextAsSasSystemUser() }
    verify(exactly = 0) { userContextService.clearContext() }
    verifyNoEventUpdatesMade()
  }

  @Test
  fun `handler returns PROCESSED, update event processed state to PROCESSED`() {
    val event = buildPendingInboxEventEntity(eventType = "test.event")

    every { inboxEventService.findPendingOldestFirst(10) } returns listOf(event)

    val handler = MockEventHandler(
      supportedEventTypes = setOf("test.event"),
      result = InboxEventHandler.Result.PROCESSED,
    )

    val stats = inboxEventDispatcher(
      handlers = listOf(handler),
      maxEventsPerBatch = 10,
    ).process()

    handler.assertThatHasProcessedEvent(event)

    assertThat(stats.processedCount).isEqualTo(1)
    assertThat(stats.ignoredCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(0)
    assertThat(stats.failedCount).isEqualTo(0)

    verify { userContextService.setUserContextAsSasSystemUser() }
    verify { inboxEventService.updateInboxEventStatusAndSave(event, ProcessedStatus.PROCESSED) }
    verify { userContextService.clearContext() }
  }

  @Test
  fun `handler supports multiple event types`() {
    val firstTypeEvent = buildPendingInboxEventEntity(eventType = "test.event1")
    val secondTypeEvent = buildPendingInboxEventEntity(eventType = "test.event2")
    val thirdTypeEvent = buildPendingInboxEventEntity(eventType = "test.event3")

    every { inboxEventService.findPendingOldestFirst(10) } returns listOf(firstTypeEvent, secondTypeEvent, thirdTypeEvent)

    val handler = MockEventHandler(
      supportedEventTypes = setOf("test.event1", "test.event2", "test.event3"),
      result = InboxEventHandler.Result.PROCESSED,
    )

    val stats = inboxEventDispatcher(
      handlers = listOf(handler),
      maxEventsPerBatch = 10,
    ).process()

    handler.assertThatHasProcessedEvent(firstTypeEvent)
    handler.assertThatHasProcessedEvent(secondTypeEvent)
    handler.assertThatHasProcessedEvent(thirdTypeEvent)

    assertThat(stats.processedCount).isEqualTo(3)
    assertThat(stats.ignoredCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(0)
    assertThat(stats.failedCount).isEqualTo(0)

    verify { inboxEventService.updateInboxEventStatusAndSave(firstTypeEvent, ProcessedStatus.PROCESSED) }
    verify { inboxEventService.updateInboxEventStatusAndSave(secondTypeEvent, ProcessedStatus.PROCESSED) }
    verify { inboxEventService.updateInboxEventStatusAndSave(thirdTypeEvent, ProcessedStatus.PROCESSED) }
  }

  @Test
  fun `two handlers declare the same event type and application should fail on construction`() {
    data class OtherMockEventHandler(
      val supportedEventTypes: Set<String>,
    ) : InboxEventHandler {
      override fun supportedEventTypes() = supportedEventTypes
      override fun handle(inboxEvent: InboxEventHandler.InboxEvent) = InboxEventHandler.Result.PROCESSED
    }

    val handlers = listOf(
      MockEventHandler(
        supportedEventTypes = setOf("test.event1", "test.event2"),
        result = InboxEventHandler.Result.PROCESSED,
      ),
      OtherMockEventHandler(supportedEventTypes = setOf("test.event2", "test.event3")),
    )

    val exception = assertThrows<IllegalArgumentException> {
      inboxEventDispatcher(handlers = handlers)
    }

    assertThat(exception.message).isEqualTo(
      "Multiple handlers registered for event type 'test.event2': MockEventHandler and OtherMockEventHandler",
    )
  }

  @Test
  fun `handler returns IGNORED, update event processed state to IGNORED`() {
    val event = buildPendingInboxEventEntity(eventType = "test.event")

    every { inboxEventService.findPendingOldestFirst(10) } returns listOf(event)

    val handler = MockEventHandler(
      supportedEventTypes = setOf("test.event"),
      result = InboxEventHandler.Result.IGNORED,
    )

    val stats = inboxEventDispatcher(
      handlers = listOf(handler),
      maxEventsPerBatch = 10,
    ).process()

    handler.assertThatHasProcessedEvent(event)

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.ignoredCount).isEqualTo(1)
    assertThat(stats.skippedCount).isEqualTo(0)
    assertThat(stats.failedCount).isEqualTo(0)

    verify { userContextService.setUserContextAsSasSystemUser() }
    verify { inboxEventService.updateInboxEventStatusAndSave(event, ProcessedStatus.IGNORED) }
    verify { userContextService.clearContext() }
  }

  @Test
  fun `handler returns FAILED, update event processed state to FAILED and raise alert`() {
    val event = buildPendingInboxEventEntity(eventType = "test.event")

    every { inboxEventService.findPendingOldestFirst(10) } returns listOf(event)

    val handler = MockEventHandler(
      supportedEventTypes = setOf("test.event"),
      result = InboxEventHandler.Result.FAILED,
    )

    val stats = inboxEventDispatcher(
      handlers = listOf(handler),
      maxEventsPerBatch = 10,
    ).process()

    handler.assertThatHasProcessedEvent(event)

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.ignoredCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(0)
    assertThat(stats.failedCount).isEqualTo(1)

    verify { userContextService.setUserContextAsSasSystemUser() }
    verify { inboxEventService.updateInboxEventStatusAndSave(event, ProcessedStatus.FAILED) }
    verify { sentryService.captureErrorMessage("Unexpected error dispatching to handler [inboxEventId=${event.id}, eventType=${event.eventType}]") }
    verify { userContextService.clearContext() }
  }

  @Test
  fun `handler throws Exception, ,update event processed state to FAILED and raise alert`() {
    val event = buildPendingInboxEventEntity(eventType = "test.event")

    every { inboxEventService.findPendingOldestFirst(10) } returns listOf(event)

    val exception = Exception("error message")

    val handler = MockEventHandler(
      supportedEventTypes = setOf("test.event"),
      responseException = exception,
      result = InboxEventHandler.Result.FAILED,
    )

    val stats = inboxEventDispatcher(
      handlers = listOf(handler),
      maxEventsPerBatch = 10,
    ).process()

    handler.assertThatHasProcessedEvent(event)

    assertThat(stats.processedCount).isEqualTo(0)
    assertThat(stats.ignoredCount).isEqualTo(0)
    assertThat(stats.skippedCount).isEqualTo(0)
    assertThat(stats.failedCount).isEqualTo(1)

    verify { inboxEventService.updateInboxEventStatusAndSave(event, ProcessedStatus.FAILED) }

    val raisedExceptionSlot = slot<InboxEventDispatcherFailureException>()
    verify { userContextService.setUserContextAsSasSystemUser() }
    verify { sentryService.captureException(capture(raisedExceptionSlot)) }
    verify { userContextService.clearContext() }

    assertThat(raisedExceptionSlot.captured.message).isEqualTo("Unexpected error dispatching to handler [inboxEventId=${event.id}, eventType=${event.eventType}]")
    assertThat(raisedExceptionSlot.captured.cause).isEqualTo(exception)
  }

  private fun inboxEventDispatcher(
    handlers: List<InboxEventHandler> = emptyList(),
    maxEventsPerBatch: Int = 1,
    maxConcurrentEvents: Int = 1,
  ) = InboxEventDispatcher(
    handlers = handlers,
    dispatcherConfig = DispatcherConfig(maxEventsPerBatch, maxConcurrentEvents),
    inboxEventService = inboxEventService,
    sentryService = sentryService,
    userContextService = userContextService,
  )

  private data class MockEventHandler(
    val supportedEventTypes: Set<String>,
    val result: InboxEventHandler.Result,
    val responseException: Throwable? = null,
    val processedEvents: MutableList<InboxEventHandler.InboxEvent> = mutableListOf(),
  ) : InboxEventHandler {
    override fun supportedEventTypes() = supportedEventTypes
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
