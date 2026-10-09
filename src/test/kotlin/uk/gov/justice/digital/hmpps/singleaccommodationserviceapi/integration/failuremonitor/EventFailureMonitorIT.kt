package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.failuremonitor

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildInboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventFailureMonitor
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.OutboxEventFailureMonitor
import java.time.Instant
import java.util.UUID

class EventFailureMonitorIT : IntegrationTestBase() {

  @Autowired
  private lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  private lateinit var outboxEventRepository: OutboxEventRepository

  @Autowired
  private lateinit var inboxEventFailureMonitor: InboxEventFailureMonitor

  @Autowired
  private lateinit var outboxEventFailureMonitor: OutboxEventFailureMonitor

  @Test
  fun `should send a Sentry alert when failed inbox events exist`() {
    inboxEventRepository.saveAll(
      listOf(
        buildInboxEventEntity(processedStatus = ProcessedStatus.FAILED),
        buildInboxEventEntity(processedStatus = ProcessedStatus.FAILED),
      ),
    )

    inboxEventFailureMonitor.reportFailedEvents()

    val failedCount = inboxEventRepository.countByProcessedStatus(ProcessedStatus.FAILED)
    assertThat(testSentryService.errorMessages).hasSize(1)
    assertThat(testSentryService.errorMessages.single()).contains("Detected $failedCount failed inbox event(s)")
  }

  @Test
  fun `should send a Sentry alert when failed outbox events exist`() {
    outboxEventRepository.saveAll(
      listOf(
        failedOutboxEvent(),
        failedOutboxEvent(),
      ),
    )

    outboxEventFailureMonitor.reportFailedEvents()

    val failedCount = outboxEventRepository.countByProcessedStatus(ProcessedStatus.FAILED)
    assertThat(testSentryService.errorMessages).hasSize(1)
    assertThat(testSentryService.errorMessages.single()).contains("Detected $failedCount failed outbox event(s)")
  }

  private fun failedOutboxEvent() = OutboxEventEntity(
    id = UUID.randomUUID(),
    aggregateId = UUID.randomUUID(),
    aggregateType = "case",
    domainEventType = "case.changed",
    payload = "{}",
    createdAt = Instant.now(),
    processedStatus = ProcessedStatus.FAILED,
    processedAt = Instant.now(),
  )
}
