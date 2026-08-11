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
    request.status = CaseRefreshRequestStatus.PENDING
    clearClaim(request)
  }

  fun releaseAfterSuccess(request: CaseRefreshRequestEntity) {
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
