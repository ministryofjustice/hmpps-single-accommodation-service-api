package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserContextService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseProjectionRefreshService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService

@Component
@ConfigurationProperties(prefix = "case-refresh.worker")
class CaseRefreshWorkerConfig(
  var maxRequestsPerRun: Int = 10,
)

@Component
@ConditionalOnProperty(
  name = ["case-refresh.worker.enabled"],
  havingValue = "true",
  matchIfMissing = true,
)
class CaseRefreshWorker(
  private val caseRefreshRequestService: CaseRefreshRequestService,
  private val caseProjectionRefreshService: CaseProjectionRefreshService,
  private val userContextService: UserContextService,
  private val config: CaseRefreshWorkerConfig,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  @Scheduled(fixedDelayString = $$"${case-refresh.worker.fixed-delay}")
  @SchedulerLock(
    name = "CaseRefreshWorker",
    lockAtMostFor = $$"${shedlock.case-refresh-worker.lock-at-most-for}",
    lockAtLeastFor = $$"${shedlock.case-refresh-worker.lock-at-least-for}",
  )
  fun process(): Stats {
    val claims = caseRefreshRequestService.claimPending(config.maxRequestsPerRun)
    var refreshedCount = 0
    var failedCount = 0

    claims.forEach { claim ->
      try {
        userContextService.setUserContextAsSasSystemUser()
        when (caseProjectionRefreshService.refresh(claim)) {
          CaseProjectionRefreshService.Result.Refreshed -> {
            refreshedCount++
          }
          CaseProjectionRefreshService.Result.UpstreamFailed -> {
            caseRefreshRequestService.returnToPending(claim)
            failedCount++
          }
          CaseProjectionRefreshService.Result.CaseNotFound -> {
            log.info("Case removed before refresh completed [caseId={}]", claim.caseId)
          }
        }
      } catch (exception: Exception) {
        failedCount++
        log.error("Unexpected error refreshing Case [caseId={}]", claim.caseId, exception)
        runCatching { caseRefreshRequestService.returnToPending(claim) }
          .onFailure { recoveryError ->
            log.error("Unable to return Case refresh request to pending [caseId={}]", claim.caseId, recoveryError)
          }
      } finally {
        userContextService.clearContext()
      }
    }

    return Stats(refreshedCount, failedCount)
  }

  data class Stats(
    val refreshedCount: Int,
    val failedCount: Int,
  )
}
