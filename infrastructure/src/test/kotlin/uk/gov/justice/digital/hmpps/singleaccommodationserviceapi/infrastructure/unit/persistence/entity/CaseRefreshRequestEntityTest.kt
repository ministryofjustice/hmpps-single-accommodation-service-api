package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.persistence.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import java.time.Instant
import java.util.UUID

class CaseRefreshRequestEntityTest {
  private val now: Instant = Instant.parse("2026-07-20T10:00:00Z")
  private val claimId: UUID = UUID.randomUUID()

  @Test
  fun `claim records ownership of the current generation`() {
    val request = refreshRequest()

    request.claim(claimId, now)

    assertThat(request.status).isEqualTo(CaseRefreshRequestStatus.PROCESSING)
    assertThat(request.processingGeneration).isEqualTo(1)
    assertThat(request.claimedAt).isEqualTo(now)
    assertThat(request.claimId).isEqualTo(claimId)
    assertThat(request.isOwnedBy(1, claimId)).isTrue()
    assertThat(request.isOwnedBy(1, UUID.randomUUID())).isFalse()
  }

  @Test
  fun `schedule retry clears claim and records next attempt with failure details`() {
    val request = refreshRequest()
    request.claim(claimId, now)
    val nextAttemptAt = now.plusSeconds(300)

    request.scheduleRetry(
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
    val request = refreshRequest(attemptCount = 2)
    request.claim(claimId, now)
    val failedAt = now.plusSeconds(1)

    request.failPermanently(
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
    val request = refreshRequest(nextAttemptAt = newerRequestAt)
    request.claim(claimId, now)
    request.generation = 2
    request.nextAttemptAt = newerRequestAt

    request.releaseForNewerGeneration()

    assertThat(request.status).isEqualTo(CaseRefreshRequestStatus.PENDING)
    assertThat(request.generation).isEqualTo(2)
    assertThat(request.nextAttemptAt).isEqualTo(newerRequestAt)
    assertThat(request.claimId).isNull()
    assertThat(request.processingGeneration).isNull()
  }

  private fun refreshRequest(
    status: CaseRefreshRequestStatus = CaseRefreshRequestStatus.PENDING,
    attemptCount: Int = 0,
    nextAttemptAt: Instant? = now,
    lastFailureCategory: CaseRefreshFailureCategory? = null,
    lastFailureDetail: String? = null,
    failedAt: Instant? = null,
  ) = CaseRefreshRequestEntity(
    caseId = UUID.randomUUID(),
    generation = 1,
    processingGeneration = null,
    status = status,
    requestedAt = now,
    claimedAt = null,
    claimId = null,
    attemptCount = attemptCount,
    nextAttemptAt = nextAttemptAt,
    lastFailureCategory = lastFailureCategory,
    lastFailureDetail = lastFailureDetail,
    failedAt = failedAt,
  )
}
