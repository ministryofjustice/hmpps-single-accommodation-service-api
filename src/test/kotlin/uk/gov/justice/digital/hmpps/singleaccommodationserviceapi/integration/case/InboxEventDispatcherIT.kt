package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPendingInboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.DispatcherConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventDispatcher
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.INBOX_EVENT
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

/**
 * Integration tests for [InboxEventDispatcher] proving:
 * - Virtual threads enable concurrent processing
 * - Events are processed in eventOccurredAt order (oldest first)
 * - maxEventsPerBatch limits batch size
 */

class InboxEventDispatcherIT : IntegrationTestBase() {
  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  lateinit var inboxEventDispatcher: InboxEventDispatcher

  @Autowired
  lateinit var jsonMapper: JsonMapper

  @Autowired
  lateinit var dispatcherConfig: DispatcherConfig

  @Autowired
  lateinit var mockEventHandler: MockInboxEventHandler

  @Autowired
  lateinit var inboxAsserter: InboxAsserter

  private val crn = UUID.randomUUID().toString()

  @BeforeEach
  fun setup() {
    HmppsAuthStubs.stubGrantToken()
    databaseUtils.truncate(INBOX_EVENT)
    dispatcherConfig.maxConcurrentEvents = 4
    dispatcherConfig.maxEventsPerBatch = 10
  }

  @Test
  fun `processes only maxEventsPerBatch events per invocation`() {
    dispatcherConfig.maxEventsPerBatch = 2

    val baseTime = OffsetDateTime.now(ZoneOffset.UTC)
    inboxEventRepository.saveAll(
      (1..5).map { i ->
        createInboxEvent(eventOccurredAt = baseTime.plusSeconds(i.toLong()), crn = UUID.randomUUID().toString())
      },
    )

    inboxEventDispatcher.process()

    inboxAsserter.assertProcessedCount(2)
    inboxAsserter.assertPendingCount(3)

    val processed = inboxEventRepository.findAllByProcessedStatus(ProcessedStatus.PROCESSED, Pageable.unpaged())

    mockEventHandler.assertThatHasProcessedEvents(processed)
  }

  @Test
  fun `processes events in eventOccurredAt ascending order`() {
    dispatcherConfig.maxEventsPerBatch = 1

    val t1 = OffsetDateTime.of(2025, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC)
    val t2 = OffsetDateTime.of(2025, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC)
    val t3 = OffsetDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)

    inboxEventRepository.saveAll(
      listOf(
        createInboxEvent(eventOccurredAt = t3, crn = UUID.randomUUID().toString()),
        createInboxEvent(eventOccurredAt = t1, crn = UUID.randomUUID().toString()),
        createInboxEvent(eventOccurredAt = t2, crn = UUID.randomUUID().toString()),
      ),
    )

    inboxEventDispatcher.process()
    val firstProcessed =
      inboxEventRepository
        .findAllByProcessedStatus(
          ProcessedStatus.PROCESSED,
          PageRequest.of(0, 10, Sort.by("processedAt").ascending()),
        )
        .single()

    inboxEventDispatcher.process()
    val secondProcessed =
      inboxEventRepository
        .findAllByProcessedStatus(
          ProcessedStatus.PROCESSED,
          PageRequest.of(0, 10, Sort.by("processedAt").ascending()),
        )
        .last()

    inboxEventDispatcher.process()
    val thirdProcessed =
      inboxEventRepository
        .findAllByProcessedStatus(
          ProcessedStatus.PROCESSED,
          PageRequest.of(0, 10, Sort.by("processedAt").ascending()),
        )
        .last()

    assertThat(firstProcessed.eventOccurredAt).isEqualTo(t1)
    assertThat(secondProcessed.eventOccurredAt).isEqualTo(t2)
    assertThat(thirdProcessed.eventOccurredAt).isEqualTo(t3)

    mockEventHandler.assertThatHasProcessedEvents(listOf(firstProcessed, secondProcessed, thirdProcessed))
  }

  @Test
  fun `processes at most 4 events concurrently due to semaphore limit`() {
    dispatcherConfig.maxEventsPerBatch = 5
    dispatcherConfig.maxConcurrentEvents = 4
    val crns = (1..5).map { "X12345$it" }

    val baseTime = OffsetDateTime.now(ZoneOffset.UTC)
    inboxEventRepository.saveAll(
      crns.mapIndexed { i, c ->
        createInboxEvent(eventOccurredAt = baseTime.plusSeconds(i.toLong()), crn = c)
      },
    )

    inboxEventDispatcher.process()

    inboxAsserter.assertProcessedCount(5)

    assertThat(mockEventHandler.maxConcurrent.get()).isEqualTo(4)
  }

  @Test
  fun `processes multiple events concurrently using coroutines`() {
    val delayMs = 200
    val crns = listOf("X123451", "X123452", "X123453", "X123454")

    val baseTime = OffsetDateTime.now(ZoneOffset.UTC)
    inboxEventRepository.saveAll(
      crns.mapIndexed { i, c ->
        createInboxEvent(eventOccurredAt = baseTime.plusSeconds(i.toLong()), crn = c)
      },
    )

    val start = System.currentTimeMillis()
    inboxEventDispatcher.process()
    val elapsed = System.currentTimeMillis() - start

    inboxAsserter.assertProcessedCount(4)

    // With 4 events and semaphore(4), parallel execution: ~delayMs. Sequential would be
    // 4*delayMs.
    assertThat(elapsed).isLessThan((delayMs * 3).toLong())
  }

  private fun createInboxEvent(
    eventOccurredAt: OffsetDateTime,
    crn: String = this.crn,
  ): InboxEventEntity {
    val payload =
      SnsDomainEvent(
        eventType = MockInboxEventHandler.EVENT_TYPE,
        version = 1,
        description = "Integration test event",
        detailUrl = "localhost",
        occurredAt = eventOccurredAt,
        personReference =
        PersonReference(
          identifiers = listOf(PersonIdentifier("CRN", crn)),
        ),
      )
    return buildPendingInboxEventEntity(
      eventType = MockInboxEventHandler.EVENT_TYPE,
      eventOccurredAt = eventOccurredAt,
      payload = jsonMapper.writeValueAsString(payload),
    )
  }
}
