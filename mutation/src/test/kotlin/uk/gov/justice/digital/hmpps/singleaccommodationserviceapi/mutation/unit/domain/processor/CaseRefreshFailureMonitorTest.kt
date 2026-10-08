package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.CaseRefreshFailureMonitor
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CaseRefreshFailureMonitorTest {

  @MockK
  lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  @RelaxedMockK
  lateinit var sentryService: SentryService

  @InjectMockKs
  lateinit var caseRefreshFailureMonitor: CaseRefreshFailureMonitor

  @Test
  fun `does nothing when there are no failed case refresh requests`() {
    every { caseRefreshRequestRepository.countByStatus(CaseRefreshRequestStatus.FAILED) } returns 0

    caseRefreshFailureMonitor.reportFailedRequests()

    verify(exactly = 0) { sentryService.captureErrorMessage(any()) }
  }

  @Test
  fun `captures one alert when failed requests exist`() {
    val oldestFailedAt = Instant.parse("2026-10-07T23:50:00Z")
    val oldest = failedRequest(
      caseId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
      failedAt = oldestFailedAt,
    )
    val newer = failedRequest(
      caseId = UUID.fromString("00000000-0000-0000-0000-000000000002"),
      failedAt = Instant.parse("2026-10-07T23:55:00Z"),
    )

    every { caseRefreshRequestRepository.countByStatus(CaseRefreshRequestStatus.FAILED) } returns 2L

    caseRefreshFailureMonitor.reportFailedRequests()

    verify {
      sentryService.captureErrorMessage(
        match {
          it.contains("Detected 2 failed case refresh request(s)")
        },
      )
    }
  }

  private fun failedRequest(
    caseId: UUID = UUID.randomUUID(),
    failedAt: Instant = Instant.parse("2026-10-07T23:50:00Z"),
  ) = CaseRefreshRequestEntity(
    caseId = caseId,
    generation = 1,
    processingGeneration = null,
    status = CaseRefreshRequestStatus.FAILED,
    priority = CaseRefreshPriority.BULK,
    requestedAt = failedAt.minusSeconds(60),
    claimedAt = null,
    claimId = null,
    attemptCount = 3,
    nextAttemptAt = null,
    lastFailureCategory = CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR,
    lastFailureDetail = "Upstream server error",
    failedAt = failedAt,
  )
}
