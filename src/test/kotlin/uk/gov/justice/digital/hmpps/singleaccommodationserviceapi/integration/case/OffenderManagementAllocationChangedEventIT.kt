package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.DomainEventIntegrationTestBase
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WiremockStubber
import java.time.OffsetDateTime
import java.util.UUID

@TestPropertySource(properties = ["scheduling.enabled=true"])
class OffenderManagementAllocationChangedEventIT : DomainEventIntegrationTestBase() {

  private val eventType = IncomingHmppsDomainEventType.OFFENDER_MANAGEMENT_ALLOCATION_CHANGED.typeName
  private val onboardedPrisonId = "SAS"
  private val nonOnboardedPrisonId = "WWI"
  val crn = "C${UUID.randomUUID()}"
  val prisonNumber = "P${UUID.randomUUID()}"
  val staffCode = 99999123L

  @BeforeEach
  fun setup() {
    HmppsAuthStubs.stubGrantToken()
    createSasSystemUser()
  }

  @Test
  fun `case should be refreshed and successfully processed when case is known`() {
    val responses = WiremockStubber().setupCaseOrchestrationStubs(crn = crn, prisonNumber = prisonNumber)
    caseRepository.save(
      buildCaseEntity(firstName = "Before", lastName = "Refresh", tierScore = null, roshLevelCode = null) {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      },
    )

    publishOffenderManagementAllocationChangedEvent(
      prisonNumber = prisonNumber,
      staffCode = staffCode,
      prisonId = onboardedPrisonId,
    )

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
    userRepository.save(
      buildUserEntity(
        username = "KNOWN_NOMIS_USER",
        authSource = AuthSource.NOMIS,
        nomisStaffId = staffCode,
      ),
    )

    val responses = WiremockStubber().setupCaseOrchestrationStubs(crn = crn, prisonNumber = prisonNumber)
    publishOffenderManagementAllocationChangedEvent(
      prisonNumber = prisonNumber,
      prisonId = nonOnboardedPrisonId,
      staffCode = staffCode,
    )

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
  fun `case should be upserted when prison is onboarded and staff code is missing`() {
    val responses = WiremockStubber().setupCaseOrchestrationStubs(crn = crn, prisonNumber = prisonNumber)
    publishOffenderManagementAllocationChangedEvent(
      prisonNumber = prisonNumber,
      prisonId = onboardedPrisonId,
      staffCode = null,
    )

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
    userRepository.save(
      buildUserEntity(
        username = "KNOWN_NOMIS_USER",
        authSource = AuthSource.NOMIS,
        nomisStaffId = staffCode,
      ),
    )

    CorePersonRecordStubs.getCorePersonRecordByPrisonNumberOKResponse(
      prisonNumber = prisonNumber,
      response = buildCorePersonRecord(
        identifiers = buildIdentifiers(
          crns = listOf("X111111", "X222222"),
          prisonNumbers = listOf(prisonNumber),
        ),
      ),
    )

    publishOffenderManagementAllocationChangedEvent(
      prisonNumber = prisonNumber,
      prisonId = nonOnboardedPrisonId,
      staffCode = staffCode,
    )

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
  fun `message should be ignored when case is not know, prison is not onboarded, and staff code is missing`() {
    val prisonNumber = "A${UUID.randomUUID().toString().take(6).uppercase()}"

    publishOffenderManagementAllocationChangedEvent(
      prisonNumber = prisonNumber,
      prisonId = nonOnboardedPrisonId,
      staffCode = null,
    )

    testInboxEventHelper.assertExpectedInboxEvents(ProcessedStatus.IGNORED, 1)
    assertThat(caseRepository.findByPrisonNumber(prisonNumber)).isNull()
    assertThat(testSentryService.exceptions).isEmpty()
  }

  private fun publishOffenderManagementAllocationChangedEvent(
    prisonNumber: String,
    prisonId: String,
    staffCode: Long?,
  ) {
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
        additionalInformation = buildMap<String, Any> {
          put("prisonId", prisonId)
          staffCode?.let { put("staffCode", it) }
        },
      ),
    )
  }
}
