package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserContextService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshFailureClassifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshProcessor
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshProperties
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService

@Component
@ConditionalOnProperty(
  name = ["case-refresh.worker.enabled"],
  havingValue = "true",
  matchIfMissing = true,
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
    val claims = caseRefreshRequestService.claimPending(
      properties.maxRequestsPerRun,
      properties.abandonedClaimTimeout,
    )
    var refreshedCount = 0
    var failedCount = 0

    claims.forEach { claim ->
      try {
        userContextService.setUserContextAsSasSystemUser()
        when (caseRefreshProcessor.process(claim)) {
          CaseRefreshProcessor.Result.Refreshed -> refreshedCount++
          CaseRefreshProcessor.Result.Failed -> failedCount++
          CaseRefreshProcessor.Result.CaseNotFound -> {
            log.info("Case removed before refresh completed caseId={}", claim.caseId)
          }
          CaseRefreshProcessor.Result.IgnoredStaleClaim -> {
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

    return Stats(refreshedCount, failedCount)
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
    val refreshedCount: Int,
    val failedCount: Int,
  )
}
