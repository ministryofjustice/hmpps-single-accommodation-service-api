package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
  private val lifecycleService = CaseRefreshRequestLifecycleService()
  private val now: Instant = Instant.parse("2026-07-20T10:00:00Z")
  private val claimId: UUID = UUID.randomUUID()

  @Test
  fun `claim records ownership of the current generation`() {
    val request = refreshRequest()

    lifecycleService.claim(request, claimId, now)

    assertThat(request.status).isEqualTo(CaseRefreshRequestStatus.PROCESSING)
    assertThat(request.processingGeneration).isEqualTo(1)
    assertThat(request.claimedAt).isEqualTo(now)
    assertThat(request.claimId).isEqualTo(claimId)
    assertThat(lifecycleService.isOwnedBy(request, claim(request, claimId))).isTrue()
    assertThat(lifecycleService.isOwnedBy(request, claim(request, UUID.randomUUID()))).isFalse()
  }

  @Test
  fun `schedule retry clears claim and records next attempt with failure details`() {
    val request = refreshRequest()
    lifecycleService.claim(request, claimId, now)
    val nextAttemptAt = now.plusSeconds(300)

    lifecycleService.scheduleRetry(
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
    lifecycleService.claim(request, claimId, now)
    val failedAt = now.plusSeconds(1)

    lifecycleService.failPermanently(
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
    lifecycleService.claim(request, claimId, now)
    request.generation = 2
    request.nextAttemptAt = newerRequestAt

    lifecycleService.releaseForNewerGeneration(request)

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
    lifecycleService.claim(request, claimId, now)
    request.generation = 2

    lifecycleService.releaseAfterSuccess(request)

    assertThat(request.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
    assertThat(request.generation).isEqualTo(2)
    assertThat(request.attemptCount).isZero()
    assertThat(request.lastFailureCategory).isNull()
    assertThat(request.lastFailureDetail).isNull()
    assertThat(request.nextAttemptAt).isEqualTo(newerRequestAt)
    assertThat(request.claimId).isNull()
    assertThat(request.processingGeneration).isNull()
  }

  @Test
  fun `claim throws when status is not PENDING`() {
    val request = refreshRequest().apply { status = CaseRefreshRequestStatus.FAILED }

    assertThatThrownBy { lifecycleService.claim(request, claimId, now) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Cannot claim request in status FAILED")
      .hasMessageContaining("only PENDING or abandoned PROCESSING requests can be claimed")
  }

  @Test
  fun `claim succeeds when reclaiming abandoned PROCESSING request`() {
    val request = refreshRequest().apply {
      status = CaseRefreshRequestStatus.PROCESSING
      processingGeneration = 1
      claimedAt = now.minusSeconds(600) // Old claim time
      claimId = UUID.randomUUID()
    }
    val newClaimId = UUID.randomUUID()
    val newClaimedAt = now

    lifecycleService.claim(request, newClaimId, newClaimedAt)

    assertThat(request.status).isEqualTo(CaseRefreshRequestStatus.PROCESSING)
    assertThat(request.claimId).isEqualTo(newClaimId)
    assertThat(request.claimedAt).isEqualTo(newClaimedAt)
  }

  @Test
  fun `release for newer generation throws when not PROCESSING`() {
    val request = refreshRequest()

    assertThatThrownBy { lifecycleService.releaseForNewerGeneration(request) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Cannot release for newer generation on status PENDING")
      .hasMessageContaining("only PROCESSING requests can be released")
  }

  @Test
  fun `release after success throws when not PROCESSING`() {
    val request = refreshRequest().apply { status = CaseRefreshRequestStatus.FAILED }

    assertThatThrownBy { lifecycleService.releaseAfterSuccess(request) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Cannot release after success on status FAILED")
  }

  @Test
  fun `schedule retry throws when not PROCESSING`() {
    val request = refreshRequest()

    assertThatThrownBy {
      lifecycleService.scheduleRetry(
        request = request,
        failureCategory = CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR,
        failureDetail = "Test",
        nextAttemptAt = now.plusSeconds(300),
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Cannot schedule retry on status PENDING")
      .hasMessageContaining("only PROCESSING requests can retry")
  }

  @Test
  fun `schedule retry throws when already FAILED`() {
    val request = refreshRequest().apply { status = CaseRefreshRequestStatus.FAILED }

    assertThatThrownBy {
      lifecycleService.scheduleRetry(
        request = request,
        failureCategory = CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR,
        failureDetail = "Test",
        nextAttemptAt = now.plusSeconds(300),
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Cannot schedule retry on status FAILED")
  }

  @Test
  fun `fail permanently throws when not PROCESSING`() {
    val request = refreshRequest()

    assertThatThrownBy {
      lifecycleService.failPermanently(
        request = request,
        failureCategory = CaseRefreshFailureCategory.UPSTREAM_TIMEOUT,
        failureDetail = "Test",
        failedAt = now,
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Cannot fail request with status PENDING")
      .hasMessageContaining("only PROCESSING requests can fail")
  }

  @Test
  fun `fail permanently throws when already FAILED`() {
    val request = refreshRequest().apply { status = CaseRefreshRequestStatus.FAILED }

    assertThatThrownBy { lifecycleService.failPermanently(request, CaseRefreshFailureCategory.UPSTREAM_TIMEOUT, "Test", now) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Cannot fail request with status FAILED")
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
