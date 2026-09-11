package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.InboxAsserter
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.OutboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.TestInboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.TestSqsDomainEventListener
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds

abstract class DomainEventIntegrationTestBase : IntegrationTestBase() {

  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  protected lateinit var testSqsDomainEventListener: TestSqsDomainEventListener

  @Autowired
  protected lateinit var testInboxEventHelper: TestInboxEventHelper

  @Autowired
  protected lateinit var outboxEventHelper: OutboxEventHelper

  @Autowired
  protected lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  protected lateinit var outboxEventRepository: OutboxEventRepository

  @Autowired
  protected lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  @Autowired
  protected lateinit var inboxAsserter: InboxAsserter

  @BeforeEach
  suspend fun reset() {
    purgeAndAwaitEmpty("sas-domain-events-queue")
    purgeAndAwaitEmpty("test-domain-events-queue")

    testSqsDomainEventListener.clearMessages()

    databaseUtils.truncate(
      DatabaseUtils.SasTables.INBOX_EVENT,
      DatabaseUtils.SasTables.OUTBOX_EVENT,
      DatabaseUtils.SasTables.SAS_CASE,
      DatabaseUtils.SasTables.SAS_CASE_REFRESH_REQUEST,
      DatabaseUtils.SasTables.SAS_USER,
      DatabaseUtils.SasTables.PROPOSED_ACCOMMODATION,
    )
  }

  private suspend fun purgeAndAwaitEmpty(queueName: String) {
    val request = hmppsQueueService.findQueueToPurge(queueName) ?: return
    hmppsQueueService.purgeQueue(request)
    await
      .atMost(ofSeconds(10))
      .pollInterval(ofMillis(100))
      .untilAsserted {
        assertThat(request.sqsClient.countAllMessagesOnQueue(request.queueUrl).get()).isZero()
      }
  }
}
