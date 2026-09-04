package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.InboxEventService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxRetentionCleanupWorker
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxRetentionProperties
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class InboxRetentionCleanupWorkerTest {

  @MockK
  lateinit var inboxEventService: InboxEventService

  @RelaxedMockK
  lateinit var sentryService: SentryService

  @MockK
  lateinit var meterRegistry: MeterRegistry

  private val fixedInstant = Instant.parse("2026-08-21T12:00:00Z")
  private val clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

  private lateinit var properties: InboxRetentionProperties
  private lateinit var worker: InboxRetentionCleanupWorker

  @BeforeEach
  fun setUp() {
    properties = InboxRetentionProperties(
      enabled = true,
      retentionDays = 30,
      batchSize = 1000,
    )
    worker = InboxRetentionCleanupWorker(
      inboxEventService = inboxEventService,
      properties = properties,
      clock = clock,
      sentryService = sentryService,
      meterRegistry = meterRegistry,
    )
  }

  @Test
  fun `returns zero stats when retention cleanup is disabled`() {
    properties.enabled = false

    val stats = worker.cleanup()

    assertThat(stats.totalDeleted).isEqualTo(0)
    assertThat(stats.batchCount).isEqualTo(0)
    verify(exactly = 0) { inboxEventService.deleteRetainedBatch(any(), any(), any()) }
  }

  @Test
  fun `throws exception when retention days is not positive`() {
    properties.retentionDays = 0

    assertThatThrownBy { worker.cleanup() }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Inbox retention days must be greater than 0")

    verify(exactly = 0) { inboxEventService.deleteRetainedBatch(any(), any(), any()) }
  }

  @Test
  fun `throws exception when batch size is not positive`() {
    properties.batchSize = -1

    assertThatThrownBy { worker.cleanup() }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Inbox retention batch size must be greater than 0")

    verify(exactly = 0) { inboxEventService.deleteRetainedBatch(any(), any(), any()) }
  }

  @Test
  fun `completes without deleting when no matching rows exist`() {
    val expectedCutoff = fixedInstant.minus(Duration.ofDays(30))
    val deletedCounter = mockk<Counter>(relaxed = true)
    val runsCounter = mockk<Counter>(relaxed = true)
    val durationTimer = mockk<Timer>(relaxed = true)

    every { meterRegistry.counter("inbox.retention.cleanup.deleted.total") } returns deletedCounter
    every { meterRegistry.counter("inbox.retention.cleanup.runs", "success", "true") } returns runsCounter
    every { meterRegistry.timer("inbox.retention.cleanup.duration", "success", "true") } returns durationTimer

    every {
      inboxEventService.deleteRetainedBatch(
        statuses = listOf(ProcessedStatus.PROCESSED, ProcessedStatus.IGNORED),
        cutoff = expectedCutoff,
        batchSize = 1000,
      )
    } returns 0

    val stats = worker.cleanup()

    assertThat(stats.totalDeleted).isEqualTo(0)
    assertThat(stats.batchCount).isEqualTo(0)
    assertThat(stats.retentionDays).isEqualTo(30)
    assertThat(stats.cutoff).isEqualTo(expectedCutoff)

    verify(exactly = 1) {
      inboxEventService.deleteRetainedBatch(
        statuses = listOf(ProcessedStatus.PROCESSED, ProcessedStatus.IGNORED),
        cutoff = expectedCutoff,
        batchSize = 1000,
      )
    }
    verify { deletedCounter.increment(0.0) }
    verify { runsCounter.increment() }
  }

  @Test
  fun `deletes rows in single batch when less than batch size`() {
    val expectedCutoff = fixedInstant.minus(Duration.ofDays(30))
    val deletedCounter = mockk<Counter>(relaxed = true)
    val runsCounter = mockk<Counter>(relaxed = true)
    val durationTimer = mockk<Timer>(relaxed = true)

    every { meterRegistry.counter("inbox.retention.cleanup.deleted.total") } returns deletedCounter
    every { meterRegistry.counter("inbox.retention.cleanup.runs", "success", "true") } returns runsCounter
    every { meterRegistry.timer("inbox.retention.cleanup.duration", "success", "true") } returns durationTimer

    every {
      inboxEventService.deleteRetainedBatch(
        statuses = listOf(ProcessedStatus.PROCESSED, ProcessedStatus.IGNORED),
        cutoff = expectedCutoff,
        batchSize = 1000,
      )
    } returns 450

    val stats = worker.cleanup()

    assertThat(stats.totalDeleted).isEqualTo(450)
    assertThat(stats.batchCount).isEqualTo(1)
    assertThat(stats.retentionDays).isEqualTo(30)
    assertThat(stats.cutoff).isEqualTo(expectedCutoff)

    verify(exactly = 1) {
      inboxEventService.deleteRetainedBatch(
        statuses = listOf(ProcessedStatus.PROCESSED, ProcessedStatus.IGNORED),
        cutoff = expectedCutoff,
        batchSize = 1000,
      )
    }
    verify { deletedCounter.increment(450.0) }
    verify { runsCounter.increment() }
  }

  @Test
  fun `deletes rows in multiple batches until no more rows remain`() {
    val expectedCutoff = fixedInstant.minus(Duration.ofDays(30))
    val deletedCounter = mockk<Counter>(relaxed = true)
    val runsCounter = mockk<Counter>(relaxed = true)
    val durationTimer = mockk<Timer>(relaxed = true)

    every { meterRegistry.counter("inbox.retention.cleanup.deleted.total") } returns deletedCounter
    every { meterRegistry.counter("inbox.retention.cleanup.runs", "success", "true") } returns runsCounter
    every { meterRegistry.timer("inbox.retention.cleanup.duration", "success", "true") } returns durationTimer

    every {
      inboxEventService.deleteRetainedBatch(
        statuses = listOf(ProcessedStatus.PROCESSED, ProcessedStatus.IGNORED),
        cutoff = expectedCutoff,
        batchSize = 1000,
      )
    } returnsMany listOf(1000, 1000, 250)

    val stats = worker.cleanup()

    assertThat(stats.totalDeleted).isEqualTo(2250)
    assertThat(stats.batchCount).isEqualTo(3)

    verify(exactly = 3) {
      inboxEventService.deleteRetainedBatch(
        statuses = listOf(ProcessedStatus.PROCESSED, ProcessedStatus.IGNORED),
        cutoff = expectedCutoff,
        batchSize = 1000,
      )
    }
    verify { deletedCounter.increment(2250.0) }
    verify { runsCounter.increment() }
  }

  @Test
  fun `captures exception to Sentry and records metric failure when database error occurs`() {
    val expectedCutoff = fixedInstant.minus(Duration.ofDays(30))
    val dbException = RuntimeException("Database connection timeout")
    val deletedCounter = mockk<Counter>(relaxed = true)
    val runsCounter = mockk<Counter>(relaxed = true)
    val durationTimer = mockk<Timer>(relaxed = true)

    every { meterRegistry.counter("inbox.retention.cleanup.deleted.total") } returns deletedCounter
    every { meterRegistry.counter("inbox.retention.cleanup.runs", "success", "false") } returns runsCounter
    every { meterRegistry.timer("inbox.retention.cleanup.duration", "success", "false") } returns durationTimer

    every {
      inboxEventService.deleteRetainedBatch(
        statuses = listOf(ProcessedStatus.PROCESSED, ProcessedStatus.IGNORED),
        cutoff = expectedCutoff,
        batchSize = 1000,
      )
    } throws dbException

    assertThatThrownBy { worker.cleanup() }
      .isSameAs(dbException)

    verify { sentryService.captureException(dbException) }
    verify { runsCounter.increment() }
  }
}
