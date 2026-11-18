package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshProperties
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@Component
class CaseRefreshQueueMonitor(
  private val caseRefreshRequestRepository: CaseRefreshRequestRepository,
  private val telemetryService: TelemetryService,
  private val caseRefreshProperties: CaseRefreshProperties,
  private val clock: Clock,
  @param:Value($$"${case-refresh.worker.cron}") private val workerCron: String,
  @param:Value($$"${case-refresh.worker.zone}") private val workerZone: String,
) {
  @Scheduled(cron = $$"${case-refresh.queue-monitor.cron}", zone = $$"${case-refresh.queue-monitor.zone}")
  @SchedulerLock(
    name = "CaseRefreshQueueMonitor",
    lockAtMostFor = $$"${shedlock.case-refresh-queue-monitor.lock-at-most-for}",
    lockAtLeastFor = $$"${shedlock.case-refresh-queue-monitor.lock-at-least-for}",
  )
  fun publishQueueStats() {
    val now = Instant.now(clock)
    val statsByPriority = caseRefreshRequestRepository
      .findQueueStats(now, now.minus(caseRefreshProperties.abandonedClaimTimeout))
      .associateBy { it.getPriority() }
    val failedCountsByPriority = caseRefreshRequestRepository.findFailedCountsByCategory()
      .groupBy { it.getPriority() }
    val workerScheduleActive = isWorkerScheduleActive(now).toString()

    CaseRefreshPriority.entries.forEach { priority ->
      val stats = statsByPriority[priority.name]
      val failedCountsByCategory = failedCountsByPriority[priority.name].orEmpty()
        .associate { it.getFailureCategory() to it.getFailed() }

      val failedByCategory = CaseRefreshFailureCategory.entries.associate { category ->
        category.measurementKey() to (failedCountsByCategory[category.name] ?: 0L).toDouble()
      }

      telemetryService.trackEvent(
        name = "CaseRefreshQueueStats",
        properties = mapOf(
          "priority" to priority.name,
          "workerScheduleActive" to workerScheduleActive,
        ),
        measurements = mapOf(
          "pendingDue" to (stats?.getPendingDue() ?: 0L).toDouble(),
          "pendingWaitingRetry" to (stats?.getPendingWaitingRetry() ?: 0L).toDouble(),
          "processing" to (stats?.getProcessing() ?: 0L).toDouble(),
          "processingAbandoned" to (stats?.getProcessingAbandoned() ?: 0L).toDouble(),
          "failed" to (stats?.getFailed() ?: 0L).toDouble(),
          "oldestDueWaitSeconds" to (stats?.getOldestDueWaitSeconds() ?: 0.0),
          "oldestDueAgeSeconds" to (stats?.getOldestDueAgeSeconds() ?: 0.0),
        ) + failedByCategory,
      )
    }
  }

  private fun isWorkerScheduleActive(now: Instant): Boolean {
    val zonedNow = now.atZone(ZoneId.of(workerZone))
    val next = CronExpression.parse(workerCron).next(zonedNow.minusSeconds(1))
    return next != null && !next.isAfter(zonedNow)
  }

  private fun CaseRefreshFailureCategory.measurementKey() = "failed" + name.split("_").joinToString("") { it.lowercase().replaceFirstChar(Char::uppercase) }
}
