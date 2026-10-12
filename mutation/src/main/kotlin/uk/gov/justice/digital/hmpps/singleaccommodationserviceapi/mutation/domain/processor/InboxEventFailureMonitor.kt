package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService

@Component
class InboxEventFailureMonitor(
  private val inboxEventRepository: InboxEventRepository,
  private val sentryService: SentryService,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  @Scheduled(cron = $$"${inbox-events.failed-monitor.cron}", zone = $$"${inbox-events.failed-monitor.zone}")
  @SchedulerLock(
    name = "InboxEventFailureMonitor",
    lockAtMostFor = $$"${shedlock.inbox-event-failure-monitor.lock-at-most-for}",
    lockAtLeastFor = $$"${shedlock.inbox-event-failure-monitor.lock-at-least-for}",
  )
  fun reportFailedEvents() {
    val failedCount = inboxEventRepository.countByProcessedStatus(ProcessedStatus.FAILED)
    if (failedCount == 0L) {
      log.debug("No failed inbox events found")
      return
    }

    val message = "Detected $failedCount failed inbox event(s)"

    log.error(message)
    sentryService.captureErrorMessage(message)
  }
}
