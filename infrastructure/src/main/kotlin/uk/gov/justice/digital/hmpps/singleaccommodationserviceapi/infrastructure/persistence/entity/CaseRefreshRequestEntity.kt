package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "case_refresh_request")
class CaseRefreshRequestEntity(
  @Id
  val caseId: UUID,
  var generation: Long,
  var processingGeneration: Long?,
  @Enumerated(EnumType.STRING)
  var status: CaseRefreshRequestStatus,
  var requestedAt: Instant,
  var claimedAt: Instant?,
  var claimId: UUID?,
  var attemptCount: Int,
  var nextAttemptAt: Instant?,
  @Enumerated(EnumType.STRING)
  var lastFailureCategory: CaseRefreshFailureCategory?,
  var lastFailureDetail: String?,
  var failedAt: Instant?,
) {
  fun claim(claimId: UUID, claimedAt: Instant) {
    status = CaseRefreshRequestStatus.PROCESSING
    processingGeneration = generation
    this.claimedAt = claimedAt
    this.claimId = claimId
  }

  fun isOwnedBy(generation: Long, claimId: UUID): Boolean = status == CaseRefreshRequestStatus.PROCESSING &&
    processingGeneration == generation &&
    this.claimId == claimId

  fun releaseForNewerGeneration() {
    status = CaseRefreshRequestStatus.PENDING
    clearClaim()
  }

  fun scheduleRetry(
    failureCategory: CaseRefreshFailureCategory,
    failureDetail: String,
    nextAttemptAt: Instant,
  ) {
    attemptCount += 1
    lastFailureCategory = failureCategory
    lastFailureDetail = failureDetail
    failedAt = null
    status = CaseRefreshRequestStatus.PENDING
    this.nextAttemptAt = nextAttemptAt
    clearClaim()
  }

  fun failPermanently(
    failureCategory: CaseRefreshFailureCategory,
    failureDetail: String,
    failedAt: Instant,
  ) {
    attemptCount += 1
    lastFailureCategory = failureCategory
    lastFailureDetail = failureDetail
    this.failedAt = failedAt
    status = CaseRefreshRequestStatus.FAILED
    nextAttemptAt = null
    clearClaim()
  }

  private fun clearClaim() {
    processingGeneration = null
    claimedAt = null
    claimId = null
  }
}

enum class CaseRefreshRequestStatus {
  PENDING,
  PROCESSING,
  FAILED,
}

enum class CaseRefreshFailureCategory {
  CURRENT_TIER_NOT_FOUND,
  UPSTREAM_CLIENT_ERROR,
  UPSTREAM_SERVER_ERROR,
  UPSTREAM_TIMEOUT,
  UPSTREAM_UNEXPECTED_ERROR,
  UNEXPECTED_ERROR,
}
