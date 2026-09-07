package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserContextService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshFailureClassifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshProcessor
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshProperties
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import java.time.Duration

@Component
@ConditionalOnProperty(
  name = ["case-refresh.enabled"],
  havingValue = "true",
)
class CaseRefreshWorker(
  private val caseRefreshRequestService: CaseRefreshRequestService,
  private val caseRefreshProcessor: CaseRefreshProcessor,
  private val userContextService: UserContextService,
  private val properties: CaseRefreshProperties,
  private val sentryService: SentryService,
  private val failureClassifier: CaseRefreshFailureClassifier,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  @Scheduled(fixedDelayString = $$"${case-refresh.worker.fixed-delay}")
  @SchedulerLock(
    name = "CaseRefreshWorker",
    lockAtMostFor = $$"${shedlock.case-refresh-worker.lock-at-most-for}",
    lockAtLeastFor = $$"${shedlock.case-refresh-worker.lock-at-least-for}",
  )
  fun process(): Stats {
    val startedAt = System.nanoTime()
    val claims = caseRefreshRequestService.claimPending(
      properties.maxRequestsPerRun,
      properties.abandonedClaimTimeout,
    )
    var refreshedCount = 0
    var failedCount = 0
    var caseNotFoundCount = 0
    var staleClaimCount = 0

    claims.forEach { claim ->
      try {
        userContextService.setUserContextAsSasSystemUser()
        when (caseRefreshProcessor.process(claim)) {
          CaseRefreshProcessor.Result.Refreshed -> refreshedCount++
          CaseRefreshProcessor.Result.Failed -> failedCount++
          CaseRefreshProcessor.Result.CaseNotFound -> {
            caseNotFoundCount++
            log.info("Case removed before refresh completed caseId={}", claim.caseId)
          }
          CaseRefreshProcessor.Result.IgnoredStaleClaim -> {
            staleClaimCount++
            log.info("Ignoring stale Case refresh claim caseId={}, claimId={}", claim.caseId, claim.claimId)
          }
        }
      } catch (exception: Exception) {
        failedCount++
        log.error(
          "Unable to process Case refresh claim caseId={}, claimId={}",
          claim.caseId,
          claim.claimId,
          exception,
        )
        sentryService.captureException(exception)
        recordUnexpectedFailure(claim, exception)
      } finally {
        userContextService.clearContext()
      }
    }

    val stats = Stats(
      claimedCount = claims.size,
      refreshedCount = refreshedCount,
      failedCount = failedCount,
      caseNotFoundCount = caseNotFoundCount,
      staleClaimCount = staleClaimCount,
      durationMillis = Duration.ofNanos(System.nanoTime() - startedAt).toMillis(),
    )
    logRunSummary(stats, claims)
    return stats
  }

  private fun logRunSummary(stats: Stats, claims: List<CaseRefreshRequestService.Claim>) {
    if (claims.isEmpty()) return

    log.info(
      "Case refresh run finished: claimed={} (live={}, bulk={}), refreshed={}, failed={}, " +
        "caseNotFound={}, staleClaim={}, durationMs={}, maxRequestsPerRun={}",
      stats.claimedCount,
      claims.count { it.priority == CaseRefreshPriority.LIVE },
      claims.count { it.priority == CaseRefreshPriority.BULK },
      stats.refreshedCount,
      stats.failedCount,
      stats.caseNotFoundCount,
      stats.staleClaimCount,
      stats.durationMillis,
      properties.maxRequestsPerRun,
    )
  }

  private fun recordUnexpectedFailure(
    claim: CaseRefreshRequestService.Claim,
    exception: Exception,
  ) {
    runCatching {
      caseRefreshRequestService.recordFailure(claim, failureClassifier.unexpected(exception))
    }.onFailure { recordingException ->
      log.error(
        "Unable to record Case refresh failure; claim will be recovered after the timeout caseId={}, claimId={}",
        claim.caseId,
        claim.claimId,
        recordingException,
      )
      sentryService.captureException(recordingException)
    }
  }

  data class Stats(
    val claimedCount: Int = 0,
    val refreshedCount: Int = 0,
    val failedCount: Int = 0,
    val caseNotFoundCount: Int = 0,
    val staleClaimCount: Int = 0,
    val durationMillis: Long = 0,
  )
}
