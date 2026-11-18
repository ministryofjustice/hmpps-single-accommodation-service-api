package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.MutableTestClock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.CaseRefreshQueueMonitor
import java.time.Instant
import java.util.UUID

class CaseRefreshQueueMonitorIT : IntegrationTestBase() {

  @Autowired
  private lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  @Autowired
  private lateinit var caseRefreshQueueMonitor: CaseRefreshQueueMonitor

  @Autowired
  private lateinit var clock: MutableTestClock

  private val now = Instant.parse("2026-11-18T10:00:00Z")

  @BeforeEach
  fun setup() {
    clock.freezeAt(now)
  }

  @AfterEach
  fun teardown() {
    clock.reset()
  }

  @Test
  fun `sends zero counts for both priorities when there are no refresh requests`() {
    caseRefreshQueueMonitor.publishQueueStats()

    val events = testTelemetryService.events
    assertThat(events.map { it.name }).containsOnly("CaseRefreshQueueStats")
    assertThat(events.map { it.properties["priority"] }).containsExactlyInAnyOrder("LIVE", "BULK")
    events.forEach { event ->
      assertThat(event.measurements.values).containsOnly(0.0)
    }
  }

  @Test
  fun `counts refresh requests by status and priority`() {
    saveRequest(priority = CaseRefreshPriority.LIVE, status = CaseRefreshRequestStatus.PENDING, nextAttemptAt = now.minusSeconds(10))
    saveRequest(priority = CaseRefreshPriority.LIVE, status = CaseRefreshRequestStatus.PENDING, nextAttemptAt = now.plusSeconds(60))
    saveRequest(priority = CaseRefreshPriority.LIVE, status = CaseRefreshRequestStatus.PROCESSING, claimedAt = now.minusSeconds(30))
    saveRequest(priority = CaseRefreshPriority.LIVE, status = CaseRefreshRequestStatus.PROCESSING, claimedAt = now.minusSeconds(3600))
    saveRequest(priority = CaseRefreshPriority.LIVE, status = CaseRefreshRequestStatus.FAILED, failureCategory = CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR)
    saveRequest(priority = CaseRefreshPriority.BULK, status = CaseRefreshRequestStatus.PENDING, nextAttemptAt = now.minusSeconds(10))
    saveRequest(priority = CaseRefreshPriority.BULK, status = CaseRefreshRequestStatus.PENDING, nextAttemptAt = now)

    caseRefreshQueueMonitor.publishQueueStats()

    assertThat(eventFor("LIVE").measurements).containsAllEntriesOf(
      mapOf(
        "pendingDue" to 1.0,
        "pendingWaitingRetry" to 1.0,
        "processing" to 2.0,
        "processingAbandoned" to 1.0,
        "failed" to 1.0,
      ),
    )
    assertThat(eventFor("BULK").measurements).containsAllEntriesOf(
      mapOf(
        "pendingDue" to 2.0,
        "pendingWaitingRetry" to 0.0,
        "processing" to 0.0,
        "processingAbandoned" to 0.0,
        "failed" to 0.0,
      ),
    )
  }

  @Test
  fun `reports the oldest due wait and age for each priority`() {
    saveRequest(priority = CaseRefreshPriority.LIVE, status = CaseRefreshRequestStatus.PENDING, requestedAt = now.minusSeconds(600), nextAttemptAt = now.minusSeconds(120))
    saveRequest(priority = CaseRefreshPriority.LIVE, status = CaseRefreshRequestStatus.PENDING, requestedAt = now.minusSeconds(60), nextAttemptAt = now.minusSeconds(30))
    saveRequest(priority = CaseRefreshPriority.LIVE, status = CaseRefreshRequestStatus.PENDING, requestedAt = now.minusSeconds(3600), nextAttemptAt = now.plusSeconds(60))
    saveRequest(priority = CaseRefreshPriority.BULK, status = CaseRefreshRequestStatus.PENDING, requestedAt = now.minusSeconds(3600), nextAttemptAt = now.plusSeconds(60))

    caseRefreshQueueMonitor.publishQueueStats()

    assertThat(eventFor("LIVE").measurements).containsEntry("oldestDueWaitSeconds", 120.0)
    assertThat(eventFor("LIVE").measurements).containsEntry("oldestDueAgeSeconds", 600.0)
    assertThat(eventFor("BULK").measurements).containsEntry("oldestDueWaitSeconds", 0.0)
    assertThat(eventFor("BULK").measurements).containsEntry("oldestDueAgeSeconds", 0.0)
  }

  @Test
  fun `counts failed refresh requests by failure category`() {
    saveRequest(priority = CaseRefreshPriority.LIVE, status = CaseRefreshRequestStatus.FAILED, failureCategory = CaseRefreshFailureCategory.UPSTREAM_TIMEOUT)
    saveRequest(priority = CaseRefreshPriority.LIVE, status = CaseRefreshRequestStatus.FAILED, failureCategory = CaseRefreshFailureCategory.UPSTREAM_TIMEOUT)
    saveRequest(priority = CaseRefreshPriority.LIVE, status = CaseRefreshRequestStatus.FAILED, failureCategory = CaseRefreshFailureCategory.CURRENT_TIER_NOT_FOUND)
    saveRequest(priority = CaseRefreshPriority.BULK, status = CaseRefreshRequestStatus.FAILED, failureCategory = CaseRefreshFailureCategory.UPSTREAM_CLIENT_ERROR)

    caseRefreshQueueMonitor.publishQueueStats()

    assertThat(eventFor("LIVE").measurements).containsAllEntriesOf(
      mapOf(
        "failed" to 3.0,
        "failedUpstreamTimeout" to 2.0,
        "failedCurrentTierNotFound" to 1.0,
        "failedUpstreamClientError" to 0.0,
      ),
    )
    assertThat(eventFor("BULK").measurements).containsAllEntriesOf(
      mapOf(
        "failed" to 1.0,
        "failedUpstreamClientError" to 1.0,
        "failedUpstreamTimeout" to 0.0,
      ),
    )
  }

  private fun eventFor(priority: String) = testTelemetryService.events.single { it.properties["priority"] == priority }

  private fun saveRequest(
    priority: CaseRefreshPriority,
    status: CaseRefreshRequestStatus,
    requestedAt: Instant = now.minusSeconds(60),
    nextAttemptAt: Instant? = null,
    claimedAt: Instant? = null,
    failureCategory: CaseRefreshFailureCategory? = null,
  ) {
    val case = caseRepository.save(buildCaseEntity { withCrn("CRN${caseRepository.count() + 1}") })
    caseRefreshRequestRepository.save(
      CaseRefreshRequestEntity(
        caseId = case.id,
        generation = 1,
        processingGeneration = claimedAt?.let { 1 },
        status = status,
        priority = priority,
        requestedAt = requestedAt,
        claimedAt = claimedAt,
        claimId = claimedAt?.let { UUID.randomUUID() },
        attemptCount = 0,
        nextAttemptAt = nextAttemptAt,
        lastFailureCategory = failureCategory,
        lastFailureDetail = failureCategory?.let { "Upstream failure" },
        failedAt = failureCategory?.let { now.minusSeconds(30) },
      ),
    )
  }
}
