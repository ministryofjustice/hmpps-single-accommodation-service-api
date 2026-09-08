package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import io.micrometer.core.instrument.MeterRegistry
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.InboxEventService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import java.time.Clock
import java.time.Duration
import java.time.Instant

@ConfigurationProperties(prefix = "hmpps.sqs.inbox-retention")
class InboxRetentionProperties(
  var enabled: Boolean = true,
  var retentionDays: Long = 30,
  var batchSize: Int = 1000,
)

@ConditionalOnProperty(
  name = ["hmpps.sqs.enabled"],
  havingValue = "true",
)
@Component
class InboxRetentionCleanupWorker(
  private val inboxEventService: InboxEventService,
  private val properties: InboxRetentionProperties,
  private val clock: Clock,
  private val sentryService: SentryService,
  @Autowired(required = false)
  private val meterRegistry: MeterRegistry? = null,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  private val purgeStatuses = listOf(ProcessedStatus.PROCESSED, ProcessedStatus.IGNORED)

  @Scheduled(cron = $$"${hmpps.sqs.inbox-retention.cron:0 0 3 * * *}")
  @SchedulerLock(
    name = "InboxRetentionCleanupWorker",
    lockAtMostFor = $$"${shedlock.inbox-retention-cleaner.lock-at-most-for:PT10M}",
    lockAtLeastFor = $$"${shedlock.inbox-retention-cleaner.lock-at-least-for:PT1M}",
  )
  fun cleanup(): Stats {
    if (!properties.enabled) {
      log.info("Inbox retention cleanup is disabled, skipping")
      return Stats(
        totalDeleted = 0,
        batchCount = 0,
        retentionDays = properties.retentionDays,
        cutoff = Instant.now(clock),
        duration = Duration.ZERO,
      )
    }

    require(properties.retentionDays > 0) {
      "Inbox retention days must be greater than 0, configured value: ${properties.retentionDays}"
    }
    require(properties.batchSize > 0) {
      "Inbox retention batch size must be greater than 0, configured value: ${properties.batchSize}"
    }

    val startTime = Instant.now(clock)
    val cutoff = startTime.minus(Duration.ofDays(properties.retentionDays))
    var totalDeleted = 0
    var batchCount = 0

    log.info(
      "Starting inbox retention cleanup [retentionDays={}, cutoff={}, batchSize={}]",
      properties.retentionDays,
      cutoff,
      properties.batchSize,
    )

    try {
      while (true) {
        val deletedInBatch = inboxEventService.deleteRetainedBatch(
          statuses = purgeStatuses,
          cutoff = cutoff,
          batchSize = properties.batchSize,
        )
        if (deletedInBatch == 0) {
          break
        }
        totalDeleted += deletedInBatch
        batchCount++

        log.debug(
          "Deleted inbox retention batch [batchNumber={}, deletedInBatch={}, runningTotal={}]",
          batchCount,
          deletedInBatch,
          totalDeleted,
        )

        if (deletedInBatch < properties.batchSize) {
          break
        }
      }

      val duration = Duration.between(startTime, Instant.now(clock))
      log.info(
        "Inbox retention cleanup completed successfully [retentionDays={}, cutoff={}, totalDeleted={}, batchCount={}, durationMs={}]",
        properties.retentionDays,
        cutoff,
        totalDeleted,
        batchCount,
        duration.toMillis(),
      )

      recordMetrics(totalDeleted, duration, success = true)

      return Stats(
        totalDeleted = totalDeleted,
        batchCount = batchCount,
        retentionDays = properties.retentionDays,
        cutoff = cutoff,
        duration = duration,
      )
    } catch (e: Exception) {
      val duration = Duration.between(startTime, Instant.now(clock))
      log.error(
        "Error during inbox retention cleanup [retentionDays={}, cutoff={}, totalDeletedSoFar={}, batchesProcessedSoFar={}]",
        properties.retentionDays,
        cutoff,
        totalDeleted,
        batchCount,
        e,
      )
      sentryService.captureException(e)
      recordMetrics(totalDeleted, duration, success = false)
      throw e
    }
  }

  private fun recordMetrics(totalDeleted: Int, duration: Duration, success: Boolean) {
    meterRegistry?.let { registry ->
      registry.counter(
        "inbox.retention.cleanup.deleted.total",
      ).increment(totalDeleted.toDouble())

      registry.counter(
        "inbox.retention.cleanup.runs",
        "success",
        success.toString(),
      ).increment()

      registry.timer(
        "inbox.retention.cleanup.duration",
        "success",
        success.toString(),
      ).record(duration)
    }
  }

  data class Stats(
    val totalDeleted: Int,
    val batchCount: Int,
    val retentionDays: Long,
    val cutoff: Instant,
    val duration: Duration,
  )
}
