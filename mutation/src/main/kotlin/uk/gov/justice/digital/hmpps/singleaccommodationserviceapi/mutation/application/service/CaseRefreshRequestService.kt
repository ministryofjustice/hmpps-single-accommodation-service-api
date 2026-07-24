package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class CaseRefreshRequestService(
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestRepository: CaseRefreshRequestRepository,
  private val retryPolicy: CaseRefreshRetryPolicy,
  private val clock: Clock,
) {

  @Transactional
  fun requestLiveRefresh(crn: String): Result {
    val caseEntity = caseRepository.findByCrn(crn) ?: return Result.CASE_NOT_FOUND
    caseRefreshRequestRepository.upsertPending(caseEntity.id, Instant.now(clock))
    return Result.REQUESTED
  }

  @Transactional
  fun claimPending(maxRequests: Int, abandonedClaimTimeout: Duration): List<Claim> {
    val claimedAt = Instant.now(clock)
    return caseRefreshRequestRepository.findClaimable(
      pendingStatus = CaseRefreshRequestStatus.PENDING,
      processingStatus = CaseRefreshRequestStatus.PROCESSING,
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
    val request = caseRefreshRequestRepository.findByCaseIdForUpdate(claim.caseId)
      ?: return FailureDisposition.IgnoredStaleClaim
    if (!request.isOwnedBy(claim.generation, claim.claimId)) {
      return FailureDisposition.IgnoredStaleClaim
    }
    if (request.generation != claim.generation) {
      request.releaseForNewerGeneration()
      return FailureDisposition.Handled
    }

    val failedAt = Instant.now(clock)
    return when (val decision = retryPolicy.decide(failure, request.attemptCount, failedAt)) {
      is CaseRefreshRetryDecision.RetryAt -> {
        request.scheduleRetry(failure.category, failure.detail, decision.nextAttemptAt)
        FailureDisposition.Handled
      }
      CaseRefreshRetryDecision.FailPermanently -> {
        request.failPermanently(failure.category, failure.detail, failedAt)
        FailureDisposition.Handled
      }
    }
  }

  data class Claim(
    val caseId: UUID,
    val generation: Long,
    val claimId: UUID,
  )

  enum class FailureDisposition {
    Handled,
    IgnoredStaleClaim,
  }

  enum class Result {
    REQUESTED,
    CASE_NOT_FOUND,
  }
}
