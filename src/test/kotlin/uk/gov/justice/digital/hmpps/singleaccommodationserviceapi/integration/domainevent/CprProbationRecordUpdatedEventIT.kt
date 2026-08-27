package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.domainevent

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import java.util.UUID

@TestPropertySource(properties = ["scheduling.enabled=true"])
class CprProbationRecordUpdatedEventIT : IntegrationTestBase() {

  @Autowired
  lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  lateinit var crn: String
  private val eventType = IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_UPDATED.typeName
  private fun eventDetailUrl() = "localhost"

  @BeforeEach
  fun setup() {
    crn = UUID.randomUUID().toString()
    HmppsAuthStubs.stubGrantToken()
    databaseUtils.truncate(*DatabaseUtils.SasTables.entries.toTypedArray())
    createSasSystemUser()
  }

  @Test
  fun `should process incoming HMPPS ${CPR_PROBATION_RECORD_UPDATED} domain events on existing record`() {
    caseRepository.save(buildCaseEntity { withCrn(crn) })

    testInboxEventHelper.publish(
      messageType = IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_UPDATED,
      crn = crn,
      detailUrl = eventDetailUrl(),
    )

    testInboxEventHelper.assertMessageProcessed()
    testInboxEventHelper.assertInboxEvent(crn, eventType, eventDetailUrl(), ProcessedStatus.PROCESSED)

    waitFor { assertThat(caseRefreshRequestRepository.findAll()).hasSize(1) }
  }

  @Test
  fun `process multiple incoming HMPPS CPR_PROBATION_RECORD_UPDATED domain events for the same CRN`() {
    caseRepository.save(buildCaseEntity { withCrn(crn) })

    repeat(3) {
      testInboxEventHelper.publish(
        messageType = IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_UPDATED,
        crn = crn,
        detailUrl = eventDetailUrl(),
      )
    }

    testInboxEventHelper.assertAllInboxMessagesProcessed(3)

    waitFor {
      val case = caseRefreshRequestRepository.findAll()
      assertThat(case.single().generation).isEqualTo(3)
    }
  }

  @Test
  fun `should not process incoming HMPPS CPR_PROBATION_RECORD_UPDATED domain events on unknown record`() {
    assertThat(caseRepository.findAll()).hasSize(0)
    testInboxEventHelper.publish(
      messageType = IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_UPDATED,
      crn = crn,
      detailUrl = eventDetailUrl(),
    )

    testInboxEventHelper.assertInboxEvent(crn, eventType, eventDetailUrl(), ProcessedStatus.IGNORED)
    assertThat(caseRefreshRequestRepository.findAll()).hasSize(0)
  }
}
