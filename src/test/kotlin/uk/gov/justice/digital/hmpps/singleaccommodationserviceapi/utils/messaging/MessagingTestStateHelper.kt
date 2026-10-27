package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.Duration.ZERO
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds

private val MESSAGING_QUEUES = listOf("sas-domain-events-queue", "test-domain-events-queue")

/**
 * Test-only helper that puts messaging related state (queues, scheduled background work, database
 * tables, WireMock) back to a clean baseline, and that can quiesce background processing.
 */
@Component
class MessagingTestStateHelper(
  private val hmppsQueueService: HmppsQueueService,
  private val testSqsDomainEventListener: TestSqsDomainEventListener,
  private val testInboxEventHelper: TestInboxEventHelper,
  private val databaseUtils: DatabaseUtils,
  private val inboxEventRepository: InboxEventRepository,
  private val outboxEventRepository: OutboxEventRepository,
  private val caseRefreshRequestRepository: CaseRefreshRequestRepository,
  private val schedulingProperties: TestSchedulingProperties,
  private val taskScheduler: PausableTaskScheduler,
) {

  fun enableProcessing() {
    schedulingProperties.enabled = true
  }

  /** Stops new scheduled work, then waits for any task that is already executing to finish. */
  fun disableProcessingAndAwaitIdle() {
    schedulingProperties.enabled = false
    awaitSchedulerIdle()
  }

  /**
   * Order matters: drain queues, wait for running tasks, wipe the tables, confirm nothing re-appeared,
   * and only then reset WireMock so late upstream calls cannot be recorded against the next test.
   * Stubs registered by test `@BeforeEach` methods run after this.
   */
  suspend fun reset() {
    MESSAGING_QUEUES.forEach { queueName ->
      hmppsQueueService.findQueueToPurge(queueName)?.let { request -> hmppsQueueService.purgeQueue(request) }
    }
    testSqsDomainEventListener.clearMessages()
    testInboxEventHelper.awaitNoMessagesInFlight()
    awaitSchedulerIdle()
    withContext(Dispatchers.IO) {
      databaseUtils.truncate(
        DatabaseUtils.SasTables.INBOX_EVENT,
        DatabaseUtils.SasTables.OUTBOX_EVENT,
        DatabaseUtils.SasTables.SAS_CASE,
        DatabaseUtils.SasTables.SAS_CASE_REFRESH_REQUEST,
        DatabaseUtils.SasTables.SAS_USER,
        DatabaseUtils.SasTables.PROPOSED_ACCOMMODATION,
      )
    }
    awaitNoPendingMessagingWork()
    sasWiremock.resetAll()
  }

  private fun awaitSchedulerIdle() = taskScheduler.awaitIdle()

  private fun awaitNoPendingMessagingWork() {
    await
      .atMost(ofSeconds(10))
      .pollDelay(ZERO)
      .pollInterval(ofMillis(20))
      .untilAsserted {
        assertThat(inboxEventRepository.count()).isZero()
        assertThat(outboxEventRepository.count()).isZero()
        assertThat(caseRefreshRequestRepository.count()).isZero()
      }
  }
}
