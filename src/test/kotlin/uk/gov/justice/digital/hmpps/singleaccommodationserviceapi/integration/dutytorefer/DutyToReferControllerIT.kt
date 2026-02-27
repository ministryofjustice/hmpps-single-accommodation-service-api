package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json.createDtrRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json.expectedCreateDtrResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json.expectedDutyToReferCreatedDomainEventJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.TestSqsDomainEventListener
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

class DutyToReferControllerIT : IntegrationTestBase() {
  @Autowired
  private lateinit var testSqsDomainEventListener: TestSqsDomainEventListener

  @Autowired
  private lateinit var dutyToReferRepository: DutyToReferRepository

  @Autowired
  private lateinit var outboxEventRepository: OutboxEventRepository

  private val crn = "FAKECRN1"

  private lateinit var beforeTest: Instant

  @BeforeEach
  fun setup() {
    beforeTest = Instant.now()
    dutyToReferRepository.deleteAll()
    outboxEventRepository.deleteAll()

    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()
  }

  @AfterEach
  fun teardown() {
    dutyToReferRepository.deleteAll()
    outboxEventRepository.deleteAll()
  }

  @Test
  fun `should get dutyToRefers for crn`() {
    restTestClient.get().uri("/cases/{crn}/dtrs", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully().expectStatus().isOk
  }

  @Test
  fun `should create duty to refer and publish sas-duty-to-refer-created event`() {
    val localAuthorityAreaId = UUID.randomUUID()

    val result = restTestClient.post().uri("/cases/$crn/dtr")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createDtrRequestBody(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = "2026-01-15",
          referenceNumber = "DTR-REF-001",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .returnResult().responseBody!!

    val persistedRecord = dutyToReferRepository.findByCrn(crn)!!
    assertPersistedDutyToRefer(persistedRecord, localAuthorityAreaId)

    assertThatJson(result).matchesExpectedJson(
      expectedCreateDtrResponseBody(
        id = persistedRecord.id,
        crn = crn,
        localAuthorityAreaId = localAuthorityAreaId,
        submissionDate = "2026-01-15",
        referenceNumber = "DTR-REF-001",
        createdBy = NAME_OF_LOGGED_IN_DELIUS_USER,
        createdAt = persistedRecord.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
      ),
    )

    assertPublishedSNSEvent(
      dutyToReferId = persistedRecord.id,
      eventType = SingleAccommodationServiceDomainEventType.SAS_DUTY_TO_REFER_CREATED,
      eventDescription = SingleAccommodationServiceDomainEventType.SAS_DUTY_TO_REFER_CREATED.typeDescription,
    )
    assertThatOutboxIsAsExpected(
      dutyToReferId = persistedRecord.id,
    )
  }

  private fun assertPersistedDutyToRefer(
    persistedRecord: DutyToReferEntity,
    localAuthorityAreaId: UUID,
  ) {
    assertThat(persistedRecord.crn).isEqualTo(crn)
    assertThat(persistedRecord.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
    assertThat(persistedRecord.referenceNumber).isEqualTo("DTR-REF-001")
    assertThat(persistedRecord.submissionDate).isEqualTo(LocalDate.of(2026, 1, 15))
    assertThat(persistedRecord.outcomeStatus).isNull()
    assertThat(persistedRecord.outcomeDate).isNull()
    assertThat(persistedRecord.createdByUserId).isEqualTo(userIdOfLoggedInDeliusUser)
    assertThat(persistedRecord.createdAt).isBetween(
      beforeTest.minusSeconds(1),
      Instant.now().plusSeconds(1),
    )
  }

  private fun assertPublishedSNSEvent(
    dutyToReferId: UUID,
    eventType: SingleAccommodationServiceDomainEventType,
    eventDescription: String,
    detailUrl: String = "http://api-host/duty-to-refers",
  ) {
    val emittedMessage = testSqsDomainEventListener.blockForMessage(eventType)
    assertThat(emittedMessage.description).isEqualTo(eventDescription)
    assertThat(emittedMessage.detailUrl).isEqualTo("$detailUrl/$dutyToReferId")
  }

  private fun assertThatOutboxIsAsExpected(dutyToReferId: UUID) {
    val outboxRecord = outboxEventRepository.findAll().first()
    assertThat(outboxRecord.aggregateId).isEqualTo(dutyToReferId)
    assertThat(outboxRecord.aggregateType).isEqualTo("DutyToRefer")
    assertThat(outboxRecord.domainEventType).isEqualTo(SingleAccommodationServiceDomainEventType.SAS_DUTY_TO_REFER_CREATED.name)
    assertThatJson(outboxRecord.payload).matchesExpectedJson(expectedDutyToReferCreatedDomainEventJson(dutyToReferId))
    assertThat(outboxRecord.processedStatus).isEqualTo(ProcessedStatus.SUCCESS)
  }
}
