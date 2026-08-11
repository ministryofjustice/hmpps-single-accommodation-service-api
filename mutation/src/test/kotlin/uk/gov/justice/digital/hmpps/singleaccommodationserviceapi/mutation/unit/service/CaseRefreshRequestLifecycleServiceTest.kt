package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestLifecycleService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import java.time.Instant
import java.util.UUID

class CaseRefreshRequestLifecycleServiceTest {
  private val lifecycle = CaseRefreshRequestLifecycleService()
  private val now: Instant = Instant.parse("2026-07-20T10:00:00Z")
  private val claimId: UUID = UUID.randomUUID()

  @Test
  fun `claim records ownership of the current generation`() {
    val request = refreshRequest()

    lifecycle.claim(request, claimId, now)

    assertThat(request.status).isEqualTo(CaseRefreshRequestStatus.PROCESSING)
    assertThat(request.processingGeneration).isEqualTo(1)
    assertThat(request.claimedAt).isEqualTo(now)
    assertThat(request.claimId).isEqualTo(claimId)
    assertThat(lifecycle.isOwnedBy(request, claim(request, claimId))).isTrue()
    assertThat(lifecycle.isOwnedBy(request, claim(request, UUID.randomUUID()))).isFalse()
  }

  @Test
  fun `schedule retry clears claim and records next attempt with failure details`() {
    val request = refreshRequest()
    lifecycle.claim(request, claimId, now)
    val nextAttemptAt = now.plusSeconds(300)

    lifecycle.scheduleRetry(
      request = request,
      failureCategory = CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR,
      failureDetail = "Tier returned 500",
      nextAttemptAt = nextAttemptAt,
    )

    assertThat(request.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
    assertThat(request.processingGeneration).isNull()
    assertThat(request.claimedAt).isNull()
    assertThat(request.claimId).isNull()
    assertThat(request.attemptCount).isEqualTo(1)
    assertThat(request.nextAttemptAt).isEqualTo(nextAttemptAt)
    assertThat(request.lastFailureCategory).isEqualTo(CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR)
    assertThat(request.lastFailureDetail).isEqualTo("Tier returned 500")
  }

  @Test
  fun `permanent failure becomes inspectable terminal work`() {
    val request = refreshRequest().apply { attemptCount = 2 }
    lifecycle.claim(request, claimId, now)
    val failedAt = now.plusSeconds(1)

    lifecycle.failPermanently(
      request = request,
      failureCategory = CaseRefreshFailureCategory.UPSTREAM_TIMEOUT,
      failureDetail = "Tier timed out",
      failedAt = failedAt,
    )

    assertThat(request.status).isEqualTo(CaseRefreshRequestStatus.FAILED)
    assertThat(request.processingGeneration).isNull()
    assertThat(request.claimedAt).isNull()
    assertThat(request.attemptCount).isEqualTo(3)
    assertThat(request.nextAttemptAt).isNull()
    assertThat(request.lastFailureCategory).isEqualTo(CaseRefreshFailureCategory.UPSTREAM_TIMEOUT)
    assertThat(request.lastFailureDetail).isEqualTo("Tier timed out")
    assertThat(request.failedAt).isEqualTo(failedAt)
  }

  @Test
  fun `release for newer generation retains the next attempt set by the trigger`() {
    val newerRequestAt = now.plusSeconds(1)
    val request = refreshRequest().apply { nextAttemptAt = newerRequestAt }
    lifecycle.claim(request, claimId, now)
    request.generation = 2
    request.nextAttemptAt = newerRequestAt

    lifecycle.releaseForNewerGeneration(request)

    assertThat(request.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
    assertThat(request.generation).isEqualTo(2)
    assertThat(request.nextAttemptAt).isEqualTo(newerRequestAt)
    assertThat(request.claimId).isNull()
    assertThat(request.processingGeneration).isNull()
  }

  @Test
  fun `release after success clears the failure history and keeps the next attempt`() {
    val newerRequestAt = now.plusSeconds(1)
    val request = refreshRequest().apply {
      attemptCount = 2
      nextAttemptAt = newerRequestAt
      lastFailureCategory = CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR
      lastFailureDetail = "Tier returned 500"
    }
    lifecycle.claim(request, claimId, now)
    request.generation = 2

    lifecycle.releaseAfterSuccess(request)

    assertThat(request.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
    assertThat(request.generation).isEqualTo(2)
    assertThat(request.attemptCount).isZero()
    assertThat(request.lastFailureCategory).isNull()
    assertThat(request.lastFailureDetail).isNull()
    assertThat(request.nextAttemptAt).isEqualTo(newerRequestAt)
    assertThat(request.claimId).isNull()
    assertThat(request.processingGeneration).isNull()
  }

  private fun claim(
    request: CaseRefreshRequestEntity,
    claimId: UUID,
  ): CaseRefreshRequestService.Claim = CaseRefreshRequestService.Claim(
    caseId = request.caseId,
    generation = request.generation,
    claimId = claimId,
  )

  private fun refreshRequest() = CaseRefreshRequestEntity(
    caseId = UUID.randomUUID(),
    generation = 1,
    processingGeneration = null,
    status = CaseRefreshRequestStatus.PENDING,
    priority = CaseRefreshPriority.LIVE,
    requestedAt = now,
    claimedAt = null,
    claimId = null,
    attemptCount = 0,
    nextAttemptAt = now,
    lastFailureCategory = null,
    lastFailureDetail = null,
    failedAt = null,
  )
}
