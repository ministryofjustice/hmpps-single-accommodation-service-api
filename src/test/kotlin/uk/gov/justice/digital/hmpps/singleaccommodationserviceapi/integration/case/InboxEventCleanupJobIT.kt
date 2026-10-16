package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.MutableTestClock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildInboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventCleanupJob
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.INBOX_EVENT
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class InboxEventCleanupJobIT : IntegrationTestBase() {
  @Autowired
  lateinit var inboxEventCleanupJob: InboxEventCleanupJob

  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  lateinit var clock: MutableTestClock

  @Value($$"${inbox-events.cleanup.cron}")
  lateinit var cron: String

  @Value($$"${inbox-events.cleanup.zone}")
  lateinit var zone: String

  @AfterEach
  fun resetClock() {
    clock.reset()
  }

  @Test
  fun `deletes only rows created strictly more than 90 days ago regardless of status`() {
    databaseUtils.truncate(INBOX_EVENT)
    val now = Instant.parse("2026-10-15T05:00:00Z")
    clock.freezeAt(now)
    val cutoff = now.minus(90, ChronoUnit.DAYS)
    val retained = ProcessedStatus.entries.flatMap { status ->
      listOf(cutoff, cutoff.plusSeconds(1), now).map { createdAt ->
        buildInboxEventEntity(
          createdAt = createdAt,
          processedStatus = status,
          eventOccurredAt = OffsetDateTime.parse("2020-01-01T00:00:00Z"),
        )
      }
    }
    val expired = ProcessedStatus.entries.map { status ->
      buildInboxEventEntity(
        createdAt = cutoff.minusSeconds(1),
        processedStatus = status,
        eventOccurredAt = OffsetDateTime.parse("2026-10-15T05:00:00Z"),
      )
    }
    inboxEventRepository.saveAll(retained + expired)

    inboxEventCleanupJob.deleteOldEvents()

    assertThat(inboxEventRepository.findAll().map { it.id }).hasSize(2)

    inboxEventCleanupJob.deleteOldEvents()

    assertThat(inboxEventRepository.count()).isEqualTo(retained.size.toLong())
  }
}
