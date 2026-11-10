package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService

@Component
class CaseRefreshFailureMonitor(
  private val caseRefreshRequestRepository: CaseRefreshRequestRepository,
  private val sentryService: SentryService,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  @Scheduled(cron = $$"${case-refresh.failed-monitor.cron}", zone = $$"${case-refresh.failed-monitor.zone}")
  @SchedulerLock(
    name = "CaseRefreshFailureMonitor",
    lockAtMostFor = $$"${shedlock.case-refresh-failure-monitor.lock-at-most-for}",
    lockAtLeastFor = $$"${shedlock.case-refresh-failure-monitor.lock-at-least-for}",
  )
  fun reportFailedRequests() {
    val failedCount = caseRefreshRequestRepository.countByStatus(CaseRefreshRequestStatus.FAILED)
    if (failedCount == 0L) {
      log.debug("No failed case refresh requests found")
      return
    }

    val message = "Detected $failedCount failed case refresh request(s)"

    log.error(message)
    sentryService.captureErrorMessage(message)
  }
}
