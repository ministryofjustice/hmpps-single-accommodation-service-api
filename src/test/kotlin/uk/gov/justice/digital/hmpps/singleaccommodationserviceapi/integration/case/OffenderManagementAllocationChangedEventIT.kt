package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WiremockStubber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.INBOX_EVENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.OUTBOX_EVENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_CASE
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_CASE_REFRESH_REQUEST
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_USER
import java.time.OffsetDateTime
import java.util.UUID

@TestPropertySource(properties = ["scheduling.enabled=true"])
class OffenderManagementAllocationChangedEventIT : IntegrationTestBase() {

  @Autowired
  private lateinit var caseRepository: CaseRepository

  private val eventType = IncomingHmppsDomainEventType.OFFENDER_MANAGEMENT_ALLOCATION_CHANGED.typeName

  @BeforeEach
  fun setup() {
    HmppsAuthStubs.stubGrantToken()
    databaseUtils.truncate(SAS_CASE, SAS_USER, INBOX_EVENT, OUTBOX_EVENT, SAS_CASE_REFRESH_REQUEST)
    createSasSystemUser()
  }

  @AfterEach
  suspend fun teardown() {
    hmppsQueueService.findQueueToPurge("sas-domain-events-queue")
      ?.let { request -> hmppsQueueService.purgeQueue(request) }
  }

  @Test
  fun `case should be refreshed and successfully processed when case is known`() {
    val crn = UUID.randomUUID().toString()
    val prisonNumber = UUID.randomUUID().toString()

    val responses = WiremockStubber().setupCaseOrchestrationStubs(crn = crn, prisonNumber = prisonNumber)
    caseRepository.save(
      buildCaseEntity(firstName = "Before", lastName = "Refresh", tierScore = null, roshLevelCode = null) {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      },
    )

    publishOffenderManagementAllocationChangedEvent(prisonNumber = prisonNumber, staffCode = "99999123")

    testInboxEventHelper.assertExpectedInboxEvents(ProcessedStatus.PROCESSED, 1)
    waitFor {
      val refreshedCase = caseRepository.findByPrisonNumber(prisonNumber)
      assertThat(refreshedCase).isNotNull()
      assertThat(refreshedCase!!.firstName).isEqualTo(responses.cpr!!.firstName)
      assertThat(refreshedCase.lastName).isEqualTo(responses.cpr!!.lastName)
      assertThat(refreshedCase.tierScore).isEqualTo(responses.tier!!.tierScore)
      assertThat(refreshedCase.roshLevelCode).isEqualTo(responses.case!!.roshLevel!!.code)
    }
    assertThat(testSentryService.exceptions).isEmpty()
  }

  @Test
  fun `case should be upserted and successfully processed when case is not known but user is`() {
    val crn = "X${UUID.randomUUID().toString().take(7)}"
    val prisonNumber = "A${UUID.randomUUID().toString().take(6).uppercase()}"
    val staffCode = "99999123"

    userRepository.save(
      buildUserEntity(
        username = "KNOWN_NOMIS_USER",
        authSource = AuthSource.NOMIS,
        nomisStaffId = staffCode.toLong(),
      ),
    )

    val responses = WiremockStubber().setupCaseOrchestrationStubs(crn = crn, prisonNumber = prisonNumber)
    publishOffenderManagementAllocationChangedEvent(prisonNumber = prisonNumber, staffCode = staffCode)

    testInboxEventHelper.assertExpectedInboxEvents(ProcessedStatus.PROCESSED, 1)
    waitFor {
      val createdCase = caseRepository.findByPrisonNumber(prisonNumber)
      assertThat(createdCase).isNotNull()
      assertThat(caseRepository.findByCrn(crn)).isNotNull()
      assertThat(createdCase!!.firstName).isEqualTo(responses.cpr!!.firstName)
    }
    assertThat(testSentryService.exceptions).isEmpty()
  }

  @Test
  fun `message should be failed when the cpr response contains 2 crns and message sent to sentry`() {
    val prisonNumber = "A${UUID.randomUUID().toString().take(6).uppercase()}"
    val staffCode = "12345678"

    userRepository.save(
      buildUserEntity(
        username = "KNOWN_NOMIS_USER",
        authSource = AuthSource.NOMIS,
        nomisStaffId = staffCode.toLong(),
      ),
    )

    CorePersonRecordStubs.getCorePersonRecordByPrisonNumberOKResponse(
      prisonNumber = prisonNumber,
      response = buildCorePersonRecord(
        identifiers = buildIdentifiers(crns = listOf("X111111", "X222222"), prisonNumbers = listOf(prisonNumber)),
      ),
    )

    publishOffenderManagementAllocationChangedEvent(prisonNumber = prisonNumber, staffCode = staffCode)

    testInboxEventHelper.assertExpectedInboxEvents(ProcessedStatus.FAILED, 1)
    waitFor {
      assertThat(testSentryService.exceptions).hasSize(1)
      assertThat(testSentryService.exceptions.single().message)
        .contains("Unexpected error dispatching to handler")
      assertThat(testSentryService.exceptions.single().cause?.message)
        .contains("This requires a single CRN in cpr identifiers for prisonNumber")
    }
    assertThat(caseRepository.findByPrisonNumber(prisonNumber)).isNull()
  }

  @Test
  fun `message should be ignored when case and user are not known`() {
    val prisonNumber = "A${UUID.randomUUID().toString().take(6).uppercase()}"

    publishOffenderManagementAllocationChangedEvent(prisonNumber = prisonNumber, staffCode = "11112222")

    testInboxEventHelper.assertExpectedInboxEvents(ProcessedStatus.IGNORED, 1)
    assertThat(caseRepository.findByPrisonNumber(prisonNumber)).isNull()
    assertThat(testSentryService.exceptions).isEmpty()
  }

  private fun publishOffenderManagementAllocationChangedEvent(prisonNumber: String, staffCode: String) {
    testInboxEventHelper.publish(
      SnsDomainEvent(
        eventType = eventType,
        version = 1,
        description = "test offender management allocation changed",
        detailUrl = null,
        occurredAt = OffsetDateTime.now(),
        personReference = PersonReference(
          identifiers = listOf(PersonIdentifier(type = "NOMS", value = prisonNumber)),
        ),
        additionalInformation = mapOf("staffCode" to staffCode),
      ),
    )
  }
}
