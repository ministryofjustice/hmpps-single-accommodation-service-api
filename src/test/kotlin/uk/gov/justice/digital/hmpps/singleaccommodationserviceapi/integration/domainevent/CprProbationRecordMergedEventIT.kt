package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.domainevent

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.DomainEventIntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.EnableMessageProcessing
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.TestInboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.TestInboxEventHelper.TestPersonIdentifier
import java.util.UUID

@EnableMessageProcessing
class CprProbationRecordMergedEventIT : DomainEventIntegrationTestBase() {

  lateinit var fromCrn: String
  private val eventType = IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_MERGED.typeName
  private val eventDetailUrl = "localhost"

  @BeforeEach
  fun setup() {
    fromCrn = UUID.randomUUID().toString()
    HmppsAuthStubs.stubGrantToken()
    databaseUtils.truncate(*DatabaseUtils.SasTables.entries.toTypedArray())
    createSasSystemUser()
  }

  @Test
  fun `should process incoming HMPPS ${CPR_PROBATION_RECORD_MERGED} domain events on existing record`() {
    caseRepository.save(buildCaseEntity { withCrn(fromCrn) })

    testInboxEventHelper.publish(
      messageType = IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_MERGED,
      personIdentifier = TestPersonIdentifier("fromCRN", fromCrn),
      detailUrl = eventDetailUrl,
    )

    testInboxEventHelper.assertMessageProcessed()
    testInboxEventHelper.assertInboxEvent(TestPersonIdentifier("fromCRN", fromCrn), eventType, eventDetailUrl, ProcessedStatus.PROCESSED)

    waitFor { assertThat(caseRefreshRequestRepository.findAll()).hasSize(1) }
  }

  @Test
  fun `process multiple incoming HMPPS CPR_PROBATION_RECORD_MERGED domain events for the same CRN`() {
    caseRepository.save(buildCaseEntity { withCrn(fromCrn) })

    repeat(3) {
      testInboxEventHelper.publish(
        messageType = IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_MERGED,
        personIdentifier = TestPersonIdentifier("fromCRN", fromCrn),
        detailUrl = eventDetailUrl,
      )
    }

    testInboxEventHelper.assertAllInboxMessagesProcessed(3)

    waitFor {
      val case = caseRefreshRequestRepository.findAll()
      assertThat(case.single().generation).isEqualTo(3)
    }
  }

  @Test
  fun `should not process incoming HMPPS CPR_PROBATION_RECORD_MERGED domain events on unknown record`() {
    assertThat(caseRepository.findAll()).hasSize(0)
    testInboxEventHelper.publish(
      messageType = IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_MERGED,
      personIdentifier = TestPersonIdentifier("fromCRN", fromCrn),
      detailUrl = eventDetailUrl,
    )

    testInboxEventHelper.assertInboxEvent(TestPersonIdentifier("fromCRN", fromCrn), eventType, eventDetailUrl, ProcessedStatus.IGNORED)
    assertThat(caseRefreshRequestRepository.findAll()).hasSize(0)
  }
}
