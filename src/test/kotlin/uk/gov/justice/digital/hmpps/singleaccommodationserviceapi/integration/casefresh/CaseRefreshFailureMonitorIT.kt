package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.casefresh

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.CaseRefreshFailureMonitor
import java.time.Instant
import java.util.UUID

class CaseRefreshFailureMonitorIT : IntegrationTestBase() {

  @Autowired
  private lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  @Autowired
  private lateinit var caseRefreshFailureMonitor: CaseRefreshFailureMonitor

  @Test
  fun `should send a Sentry alert when failed case refresh requests exist`() {
    val oldestFailedAt = Instant.parse("2026-10-07T23:50:00Z")
    val newerFailedAt = Instant.parse("2026-10-07T23:55:00Z")
    val firstCase = caseRepository.save(buildCaseEntity { withCrn("CRN-ALERT-1") })
    val secondCase = caseRepository.save(buildCaseEntity { withCrn("CRN-ALERT-2") })

    caseRefreshRequestRepository.saveAll(
      listOf(
        failedRequest(
          caseId = firstCase.id,
          failedAt = oldestFailedAt,
        ),
        failedRequest(
          caseId = secondCase.id,
          failedAt = newerFailedAt,
        ),
      ),
    )

    caseRefreshFailureMonitor.reportFailedRequests()

    assertThat(testSentryService.errorMessages).hasSize(1)
    assertThat(testSentryService.errorMessages.single()).contains("Detected 2 failed case refresh request(s)")
  }

  private fun failedRequest(
    caseId: UUID,
    failedAt: Instant,
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
