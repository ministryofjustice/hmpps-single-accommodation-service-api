package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class InboxEventCleanupJob(
  private val inboxEventRepository: InboxEventRepository,
  private val clock: Clock,
  @param:Value($$"${inbox-events.cleanup.retention-days}") private val retentionDays: Long,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  @Scheduled(cron = $$"${inbox-events.cleanup.cron}", zone = $$"${inbox-events.cleanup.zone}")
  @SchedulerLock(
    name = "InboxEventCleanup",
    lockAtMostFor = $$"${shedlock.inbox-event-cleanup.lock-at-most-for}",
    lockAtLeastFor = $$"${shedlock.inbox-event-cleanup.lock-at-least-for}",
  )
  fun deleteOldEvents() {
    val cutoff = Instant.now(clock).minus(retentionDays, ChronoUnit.DAYS)
    val deletedCount = inboxEventRepository.deleteCreatedBefore(cutoff)
    log.info("Deleted {} inbox event(s) created before {}", deletedCount, cutoff)
  }
}
