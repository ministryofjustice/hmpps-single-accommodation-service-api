package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import java.time.Instant
import java.util.UUID

@Service
class CaseRefreshRequestLifecycleService {
  fun claim(request: CaseRefreshRequestEntity, claimId: UUID, claimedAt: Instant) {
    require(
      request.status == CaseRefreshRequestStatus.PENDING ||
        request.status == CaseRefreshRequestStatus.PROCESSING,
    ) {
      "Cannot claim request in status ${request.status}; only PENDING or abandoned PROCESSING requests can be claimed"
    }
    request.status = CaseRefreshRequestStatus.PROCESSING
    request.processingGeneration = request.generation
    request.claimedAt = claimedAt
    request.claimId = claimId
  }

  fun isOwnedBy(
    request: CaseRefreshRequestEntity,
    claim: CaseRefreshRequestService.Claim,
  ): Boolean = request.status == CaseRefreshRequestStatus.PROCESSING &&
    request.processingGeneration == claim.generation &&
    request.claimId == claim.claimId

  fun releaseForNewerGeneration(request: CaseRefreshRequestEntity) {
    require(request.status == CaseRefreshRequestStatus.PROCESSING) {
      "Cannot release for newer generation on status ${request.status}; only PROCESSING requests can be released"
    }
    requireNotNull(request.nextAttemptAt) { "Must have next attempt set" }
    request.status = CaseRefreshRequestStatus.PENDING
    clearClaim(request)
  }

  fun releaseAfterSuccess(request: CaseRefreshRequestEntity) {
    require(request.status == CaseRefreshRequestStatus.PROCESSING) {
      "Cannot release after success on status ${request.status}; only PROCESSING requests can be released"
    }
    requireNotNull(request.nextAttemptAt) { "Must have next attempt set" }
    request.status = CaseRefreshRequestStatus.PENDING
    request.attemptCount = 0
    request.lastFailureCategory = null
    request.lastFailureDetail = null
    clearClaim(request)
  }

  fun scheduleRetry(
    request: CaseRefreshRequestEntity,
    failureCategory: CaseRefreshFailureCategory,
    failureDetail: String,
    nextAttemptAt: Instant,
  ) {
    require(request.status == CaseRefreshRequestStatus.PROCESSING) {
      "Cannot schedule retry on status ${request.status}; only PROCESSING requests can retry"
    }
    request.attemptCount += 1
    request.lastFailureCategory = failureCategory
    request.lastFailureDetail = failureDetail
    request.failedAt = null
    request.status = CaseRefreshRequestStatus.PENDING
    request.nextAttemptAt = nextAttemptAt
    clearClaim(request)
  }

  fun failPermanently(
    request: CaseRefreshRequestEntity,
    failureCategory: CaseRefreshFailureCategory,
    failureDetail: String,
    failedAt: Instant,
  ) {
    require(request.status == CaseRefreshRequestStatus.PROCESSING) {
      "Cannot fail request with status ${request.status}; only PROCESSING requests can fail"
    }
    request.attemptCount += 1
    request.lastFailureCategory = failureCategory
    request.lastFailureDetail = failureDetail
    request.failedAt = failedAt
    request.status = CaseRefreshRequestStatus.FAILED
    request.nextAttemptAt = null
    clearClaim(request)
  }

  private fun clearClaim(request: CaseRefreshRequestEntity) {
    request.processingGeneration = null
    request.claimedAt = null
    request.claimId = null
  }
}
