package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.MutableTestClock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildInboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxRetentionCleanupWorker
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxRetentionProperties
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.INBOX_EVENT
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class InboxRetentionCleanupWorkerIT : IntegrationTestBase() {

  @Autowired
  lateinit var inboxRetentionCleanupWorker: InboxRetentionCleanupWorker

  @Autowired
  lateinit var properties: InboxRetentionProperties

  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  lateinit var clock: MutableTestClock

  private val now = Instant.parse("2026-08-21T12:00:00Z")

  @BeforeEach
  fun setup() {
    clock.freezeAt(now)
    databaseUtils.truncate(INBOX_EVENT)
    properties.enabled = true
    properties.retentionDays = 30
    properties.batchSize = 1000
  }

  @AfterEach
  fun teardown() {
    clock.reset()
    properties.retentionDays = 30
    properties.batchSize = 1000
    properties.enabled = true
  }

  @Test
  fun `deletes PROCESSED and IGNORED events older than retention window while preserving recent and active events`() {
    val expiredProcessed = createEvent(ProcessedStatus.PROCESSED, processedAt = now.minus(Duration.ofDays(31)))
    val expiredIgnored = createEvent(ProcessedStatus.IGNORED, processedAt = now.minus(Duration.ofDays(45)))

    val recentProcessed = createEvent(ProcessedStatus.PROCESSED, processedAt = now.minus(Duration.ofDays(10)))
    val recentIgnored = createEvent(ProcessedStatus.IGNORED, processedAt = now.minus(Duration.ofDays(5)))

    val oldPending = createEvent(ProcessedStatus.PENDING, processedAt = null, createdAt = now.minus(Duration.ofDays(60)))
    val oldFailed = createEvent(ProcessedStatus.FAILED, processedAt = now.minus(Duration.ofDays(60)))

    inboxEventRepository.saveAll(
      listOf(
        expiredProcessed,
        expiredIgnored,
        recentProcessed,
        recentIgnored,
        oldPending,
        oldFailed,
      ),
    )

    val stats = inboxRetentionCleanupWorker.cleanup()

    assertThat(stats.totalDeleted).isEqualTo(2)
    assertThat(stats.batchCount).isEqualTo(1)
    assertThat(stats.retentionDays).isEqualTo(30)
    assertThat(stats.cutoff).isEqualTo(now.minus(Duration.ofDays(30)))

    val remainingEvents = inboxEventRepository.findAll()
    assertThat(remainingEvents.map { it.id }).containsExactlyInAnyOrder(
      recentProcessed.id,
      recentIgnored.id,
      oldPending.id,
      oldFailed.id,
    )
  }

  @Test
  fun `deletes in multiple batches when volume exceeds configured batch size`() {
    properties.batchSize = 2

    val expiredEvents = (1..5).map {
      createEvent(ProcessedStatus.PROCESSED, processedAt = now.minus(Duration.ofDays(31 + it.toLong())))
    }
    inboxEventRepository.saveAll(expiredEvents)

    val stats = inboxRetentionCleanupWorker.cleanup()

    assertThat(stats.totalDeleted).isEqualTo(5)
    assertThat(stats.batchCount).isEqualTo(3)

    val remainingEvents = inboxEventRepository.findAll()
    assertThat(remainingEvents).isEmpty()
  }

  @Test
  fun `is idempotent when there are no eligible records to purge`() {
    val stats = inboxRetentionCleanupWorker.cleanup()

    assertThat(stats.totalDeleted).isEqualTo(0)
    assertThat(stats.batchCount).isEqualTo(0)
  }

  @Test
  fun `respects custom retention days configuration`() {
    properties.retentionDays = 7

    val processed8DaysAgo = createEvent(ProcessedStatus.PROCESSED, processedAt = now.minus(Duration.ofDays(8)))
    val processed3DaysAgo = createEvent(ProcessedStatus.PROCESSED, processedAt = now.minus(Duration.ofDays(3)))

    inboxEventRepository.saveAll(listOf(processed8DaysAgo, processed3DaysAgo))

    val stats = inboxRetentionCleanupWorker.cleanup()

    assertThat(stats.totalDeleted).isEqualTo(1)
    assertThat(stats.retentionDays).isEqualTo(7)

    val remaining = inboxEventRepository.findAll()
    assertThat(remaining.map { it.id }).containsExactly(processed3DaysAgo.id)
  }

  private fun createEvent(
    status: ProcessedStatus,
    processedAt: Instant?,
    createdAt: Instant = now.minus(Duration.ofDays(40)),
  ): InboxEventEntity = buildInboxEventEntity(
    eventType = "tier.calculation.changed",
    eventOccurredAt = OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC),
    createdAt = createdAt,
    processedStatus = status,
    processedAt = processedAt,
    payload = "{}",
  )
}
