package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshFailedCountRow
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshQueueStatsRow
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshProperties
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.CaseRefreshQueueMonitor
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class CaseRefreshQueueMonitorTest {

  @MockK
  lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  @MockK
  lateinit var telemetryService: TelemetryService

  private val now = Instant.parse("2026-11-18T10:00:00Z")
  private val events = mutableListOf<TrackedEvent>()

  @BeforeEach
  fun setUp() {
    every { caseRefreshRequestRepository.findQueueStats(any(), any()) } returns emptyList()
    every { caseRefreshRequestRepository.findFailedCountsByCategory() } returns emptyList()
    every { telemetryService.trackEvent(any(), any(), any()) } answers {
      events.add(TrackedEvent(firstArg(), secondArg(), thirdArg()))
    }
  }

  @Test
  fun `sends an event with zero counts for each priority when the queue is empty`() {
    monitorAt(now).publishQueueStats()

    assertThat(events.map { it.name }).containsOnly("CaseRefreshQueueStats")
    assertThat(events.map { it.properties["priority"] }).containsExactlyInAnyOrder("LIVE", "BULK")
    events.forEach { event ->
      assertThat(event.measurements).hasSize(13)
      assertThat(event.measurements.values).containsOnly(0.0)
    }
  }

  @Test
  fun `maps the queue stats for each priority to measurements`() {
    every { caseRefreshRequestRepository.findQueueStats(any(), any()) } returns listOf(
      queueStatsRow(
        priority = "LIVE",
        pendingDue = 1,
        pendingWaitingRetry = 2,
        processing = 3,
        processingAbandoned = 4,
        failed = 5,
        oldestDueWaitSeconds = 6.5,
        oldestDueAgeSeconds = 7.5,
      ),
      queueStatsRow(priority = "BULK", pendingDue = 10),
    )

    monitorAt(now).publishQueueStats()

    assertThat(eventFor("LIVE").measurements).containsAllEntriesOf(
      mapOf(
        "pendingDue" to 1.0,
        "pendingWaitingRetry" to 2.0,
        "processing" to 3.0,
        "processingAbandoned" to 4.0,
        "failed" to 5.0,
        "oldestDueWaitSeconds" to 6.5,
        "oldestDueAgeSeconds" to 7.5,
      ),
    )
    assertThat(eventFor("BULK").measurements).containsEntry("pendingDue", 10.0)
    assertThat(eventFor("BULK").measurements).containsEntry("failed", 0.0)
  }

  @Test
  fun `sends a failed count for every failure category, including zeros`() {
    every { caseRefreshRequestRepository.findFailedCountsByCategory() } returns listOf(
      failedCountRow(priority = "LIVE", failureCategory = "UPSTREAM_TIMEOUT", failed = 2),
      failedCountRow(priority = "LIVE", failureCategory = "CURRENT_TIER_NOT_FOUND", failed = 1),
      failedCountRow(priority = "BULK", failureCategory = "UPSTREAM_TIMEOUT", failed = 4),
    )

    monitorAt(now).publishQueueStats()

    assertThat(eventFor("LIVE").measurements).containsAllEntriesOf(
      mapOf(
        "failedCurrentTierNotFound" to 1.0,
        "failedUpstreamClientError" to 0.0,
        "failedUpstreamServerError" to 0.0,
        "failedUpstreamTimeout" to 2.0,
        "failedUpstreamUnexpectedError" to 0.0,
        "failedUnexpectedError" to 0.0,
      ),
    )
    assertThat(eventFor("BULK").measurements).containsEntry("failedUpstreamTimeout", 4.0)
    assertThat(eventFor("BULK").measurements).containsEntry("failedCurrentTierNotFound", 0.0)
  }

  @Test
  fun `asks the repository for claims abandoned before the abandoned claim timeout`() {
    monitorAt(now).publishQueueStats()

    verify { caseRefreshRequestRepository.findQueueStats(now, Instant.parse("2026-11-18T09:50:00Z")) }
  }

  @ParameterizedTest
  @ValueSource(strings = ["2026-11-18T08:00:00Z", "2026-11-18T17:59:59Z", "2026-07-01T07:00:00Z", "2026-07-01T16:59:59Z"])
  fun `reports the worker schedule as active inside the worker cron window`(instant: String) {
    monitorAt(Instant.parse(instant)).publishQueueStats()

    assertThat(events.map { it.properties["workerScheduleActive"] }).containsOnly("true")
  }

  @ParameterizedTest
  @ValueSource(strings = ["2026-11-18T07:59:59Z", "2026-11-18T18:00:00Z", "2026-07-01T06:59:59Z", "2026-07-01T17:00:00Z"])
  fun `reports the worker schedule as inactive outside the worker cron window`(instant: String) {
    monitorAt(Instant.parse(instant)).publishQueueStats()

    assertThat(events.map { it.properties["workerScheduleActive"] }).containsOnly("false")
  }

  private fun monitorAt(instant: Instant) = CaseRefreshQueueMonitor(
    caseRefreshRequestRepository = caseRefreshRequestRepository,
    telemetryService = telemetryService,
    caseRefreshProperties = CaseRefreshProperties(abandonedClaimTimeout = Duration.ofMinutes(10)),
    clock = Clock.fixed(instant, ZoneOffset.UTC),
    workerCron = "* * 8-17 * * *",
    workerZone = "Europe/London",
  )

  private fun eventFor(priority: String) = events.single { it.properties["priority"] == priority }

  private fun queueStatsRow(
    priority: String,
    pendingDue: Long = 0,
    pendingWaitingRetry: Long = 0,
    processing: Long = 0,
    processingAbandoned: Long = 0,
    failed: Long = 0,
    oldestDueWaitSeconds: Double = 0.0,
    oldestDueAgeSeconds: Double = 0.0,
  ) = mockk<CaseRefreshQueueStatsRow> {
    every { getPriority() } returns priority
    every { getPendingDue() } returns pendingDue
    every { getPendingWaitingRetry() } returns pendingWaitingRetry
    every { getProcessing() } returns processing
    every { getProcessingAbandoned() } returns processingAbandoned
    every { getFailed() } returns failed
    every { getOldestDueWaitSeconds() } returns oldestDueWaitSeconds
    every { getOldestDueAgeSeconds() } returns oldestDueAgeSeconds
  }

  private fun failedCountRow(priority: String, failureCategory: String?, failed: Long) = mockk<CaseRefreshFailedCountRow> {
    every { getPriority() } returns priority
    every { getFailureCategory() } returns failureCategory
    every { getFailed() } returns failed
  }

  private data class TrackedEvent(
    val name: String,
    val properties: Map<String, String?>,
    val measurements: Map<String, Double?>,
  )
}
