package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.CaseRefreshWorker
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

@ConditionalOnBean(CaseRefreshWorker::class)
@Service
class CaseRefreshRequestService(
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestRepository: CaseRefreshRequestRepository,
  private val retryPolicy: CaseRefreshRetryPolicy,
  private val clock: Clock,
) {

  @Transactional
  fun requestLiveRefresh(caseId: UUID) = caseRefreshRequestRepository.upsertLiveRequest(caseId, Instant.now(clock))

  // entry point for a bulk refresh (case list pre-load) & not triggered yet
  @Transactional
  fun requestBulkRefresh(caseIds: List<UUID>) {
    if (caseIds.isEmpty()) return
    caseRefreshRequestRepository.insertBulkRequests(
      caseIds.map(UUID::toString).toTypedArray(),
      Instant.now(clock),
    )
  }

  @Transactional
  fun claimPending(maxRequests: Int, abandonedClaimTimeout: Duration): List<Claim> {
    val claimedAt = Instant.now(clock)
    return caseRefreshRequestRepository.findClaimable(
      pendingStatus = CaseRefreshRequestStatus.PENDING,
      processingStatus = CaseRefreshRequestStatus.PROCESSING,
      livePriority = CaseRefreshPriority.LIVE,
      now = claimedAt,
      abandonedClaimedBefore = claimedAt.minus(abandonedClaimTimeout),
      pageable = PageRequest.of(0, maxRequests),
    ).map { request ->
      val claimId = UUID.randomUUID()
      request.claim(claimId, claimedAt)
      Claim(request.caseId, request.generation, claimId)
    }
  }

  @Transactional
  fun recordFailure(
    claim: Claim,
    failure: CaseRefreshFailure,
  ): FailureDisposition {
    val request = caseRefreshRequestRepository.findByCaseId(claim.caseId)
      ?: return FailureDisposition.IGNORED_STALE_CLAIM
    if (!request.isOwnedBy(claim.generation, claim.claimId)) {
      return FailureDisposition.IGNORED_STALE_CLAIM
    }
    if (request.generation != claim.generation) {
      request.releaseForNewerGeneration()
      return FailureDisposition.HANDLED
    }

    val failedAt = Instant.now(clock)
    return when (val decision = retryPolicy.decide(failure, request.attemptCount, failedAt)) {
      is CaseRefreshRetryDecision.RetryAt -> {
        request.scheduleRetry(failure.category, failure.detail, decision.nextAttemptAt)
        FailureDisposition.HANDLED
      }
      CaseRefreshRetryDecision.FailPermanently -> {
        request.failPermanently(failure.category, failure.detail, failedAt)
        FailureDisposition.HANDLED
      }
    }
  }

  data class Claim(
    val caseId: UUID,
    val generation: Long,
    val claimId: UUID,
  )

  enum class FailureDisposition {
    HANDLED,
    IGNORED_STALE_CLAIM,
  }

  enum class Result {
    REQUESTED,
    CASE_NOT_FOUND,
  }
}
