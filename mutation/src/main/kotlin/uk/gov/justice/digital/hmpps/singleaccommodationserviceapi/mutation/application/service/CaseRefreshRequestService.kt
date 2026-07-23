package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

@ConditionalOnProperty(
  name = ["case-refresh.enabled"],
  havingValue = "true",
)
@Service
class CaseRefreshRequestService(
  private val caseRefreshRequestRepository: CaseRefreshRequestRepository,
  private val lifecycleService: CaseRefreshRequestLifecycleService,
  private val retryPolicy: CaseRefreshRetryPolicy,
  private val clock: Clock,
) {

  private val log = LoggerFactory.getLogger(this.javaClass)

  @Transactional
  fun requestLiveRefresh(caseId: UUID) {
    log.info("Requesting refresh for case [id=$caseId]")
    caseRefreshRequestRepository.upsertRequest(
      caseId = caseId,
      priority = CaseRefreshPriority.LIVE,
      requestedAt = Instant.now(clock),
    )
  }

  // entry point for a bulk refresh (case list pre-load) & not triggered yet
  @Transactional
  fun requestBulkRefresh(caseIds: List<UUID>) {
    if (caseIds.isEmpty()) return
    caseRefreshRequestRepository.insertBulkRequests(
      caseIds = caseIds.toTypedArray(),
      priority = CaseRefreshPriority.BULK,
      requestedAt = Instant.now(clock),
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
      lifecycleService.claim(request, claimId, claimedAt)
      Claim(request.caseId, request.generation, claimId)
    }
  }

  @Transactional
  fun findOwnedRequest(claim: Claim): CaseRefreshRequestEntity? {
    val request = caseRefreshRequestRepository.findByCaseId(claim.caseId) ?: return null
    return request.takeIf { lifecycleService.isOwnedBy(it, claim) }
  }

  @Transactional
  fun recordFailure(
    claim: Claim,
    failure: CaseRefreshFailure,
  ): FailureDisposition = when (val request = findOwnedRequest(claim)) {
    null -> FailureDisposition.IGNORED_STALE_CLAIM

    else -> {
      if (request.generation != claim.generation) {
        lifecycleService.releaseForNewerGeneration(request)
        FailureDisposition.HANDLED
      } else {
        val failedAt = Instant.now(clock)
        when (val decision = retryPolicy.decide(failure, request.attemptCount, failedAt)) {
          is CaseRefreshRetryDecision.RetryAt -> {
            lifecycleService.scheduleRetry(request, failure.category, failure.detail, decision.nextAttemptAt)
            FailureDisposition.HANDLED
          }

          CaseRefreshRetryDecision.FailPermanently -> {
            lifecycleService.failPermanently(request, failure.category, failure.detail, failedAt)
            FailureDisposition.HANDLED
          }
        }
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
}
