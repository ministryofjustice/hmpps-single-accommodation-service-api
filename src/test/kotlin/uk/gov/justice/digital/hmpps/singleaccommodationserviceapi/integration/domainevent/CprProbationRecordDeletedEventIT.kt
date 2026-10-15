package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.domainevent

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.DomainEventIntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import java.util.UUID

@TestPropertySource(properties = ["scheduling.enabled=true"])
class CprProbationRecordDeletedEventIT : DomainEventIntegrationTestBase() {

  lateinit var crn: String
  private val eventType = IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_DELETED.typeName
  private val eventDetailUrl = "localhost"

  @BeforeEach
  fun setup() {
    crn = UUID.randomUUID().toString()
    HmppsAuthStubs.stubGrantToken()
    databaseUtils.truncate(*DatabaseUtils.SasTables.entries.toTypedArray())
    createSasSystemUser()
  }

  @Test
  fun `should process incoming HMPPS CPR_PROBATION_RECORD_DELETED domain events on existing record`() {
    caseRepository.save(buildCaseEntity { withCrn(crn) })

    testInboxEventHelper.publish(
      messageType = IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_DELETED,
      crn = crn,
      detailUrl = eventDetailUrl,
    )

    testInboxEventHelper.assertMessageProcessed()
    testInboxEventHelper.assertInboxEvent(crn, eventType, eventDetailUrl, ProcessedStatus.PROCESSED)

    waitFor {
      val updatedCase = caseRepository.findByCrn(crn)
      assertThat(updatedCase).isNull()
    }
  }

  @Test
  fun `should not process incoming HMPPS CPR_PROBATION_RECORD_DELETED domain events on unknown record`() {
    assertThat(caseRepository.findAll()).hasSize(0)
    testInboxEventHelper.publish(
      messageType = IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_DELETED,
      crn = crn,
      detailUrl = eventDetailUrl,
    )

    testInboxEventHelper.assertInboxEvent(crn, eventType, eventDetailUrl, ProcessedStatus.IGNORED)
    assertThat(caseRepository.findAll()).hasSize(0)
  }

  @Test
  fun `should not process incoming HMPPS CPR_PROBATION_RECORD_DELETED domain events on archived record`() {
    val case = caseRepository.save(buildCaseEntity { withCrn(crn) })
    assertThat(caseRepository.findAll()).hasSize(1)
    case.archiveCrn(crn)
    caseRepository.saveAndFlush(case)
    assertThat(caseRepository.findAllByIdentifiers(crns = listOf(crn), prisonNumbers = emptyList())).hasSize(0)
    assertThat(caseRepository.findAll()).hasSize(0)

    testInboxEventHelper.publish(
      messageType = IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_DELETED,
      crn = crn,
      detailUrl = eventDetailUrl,
    )

    testInboxEventHelper.assertInboxEvent(crn, eventType, eventDetailUrl, ProcessedStatus.IGNORED)
    assertThat(caseRepository.findAll()).hasSize(0)
  }
}
