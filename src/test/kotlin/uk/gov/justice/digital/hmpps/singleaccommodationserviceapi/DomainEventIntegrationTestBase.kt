package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.InboxAsserter
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.MessagingTestStateHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.OutboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.TestInboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.TestSqsDomainEventListener
import uk.gov.justice.hmpps.sqs.HmppsQueueService

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

  @Autowired
  private lateinit var messagingTestStateHelper: MessagingTestStateHelper

  @BeforeEach
  suspend fun reset() {
    messagingTestStateHelper.reset()
  }
}
