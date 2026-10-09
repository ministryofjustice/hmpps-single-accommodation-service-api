package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService

@Component
class OutboxEventFailureMonitor(
  private val outboxEventRepository: OutboxEventRepository,
  private val sentryService: SentryService,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  @Scheduled(cron = $$"${outbox-events.failed-monitor.cron}", zone = $$"${outbox-events.failed-monitor.zone}")
  @SchedulerLock(
    name = "OutboxEventFailureMonitor",
    lockAtMostFor = $$"${shedlock.outbox-event-failure-monitor.lock-at-most-for}",
    lockAtLeastFor = $$"${shedlock.outbox-event-failure-monitor.lock-at-least-for}",
  )
  fun reportFailedEvents() {
    val failedCount = outboxEventRepository.countByProcessedStatus(ProcessedStatus.FAILED)
    if (failedCount == 0L) {
      log.debug("No failed outbox events found")
      return
    }

    val message = "Detected $failedCount failed outbox event(s)"

    log.error(message)
    sentryService.captureErrorMessage(message)
  }
}
