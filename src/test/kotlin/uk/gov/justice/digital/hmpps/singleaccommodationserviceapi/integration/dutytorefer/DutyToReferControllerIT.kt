package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_TEST_DATA_SETUP_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json.createDtrRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json.expectedDtrResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json.expectedDutyToReferUpdatedDomainEventJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json.expectedNotStartedDtrResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.TestSqsDomainEventListener
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus as EntityDtrStatus

class DutyToReferControllerIT : IntegrationTestBase() {
  @Autowired
  private lateinit var testSqsDomainEventListener: TestSqsDomainEventListener

  @Autowired
  private lateinit var dutyToReferRepository: DutyToReferRepository

  @Autowired
  private lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  @Autowired
  private lateinit var outboxEventRepository: OutboxEventRepository

  @Autowired
  private lateinit var caseRepository: CaseRepository

  private lateinit var crn: String
  private lateinit var case: CaseEntity

  private lateinit var beforeTest: Instant

  @BeforeEach
  fun setup() {
    beforeTest = Instant.now()
    dutyToReferRepository.deleteAll()
    outboxEventRepository.deleteAll()
    case = caseRepository.save(buildCaseEntity())
    crn = case.caseIdentifiers.first().identifier
    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()
  }

  @AfterEach
  fun teardown() {
    dutyToReferRepository.deleteAll()
    outboxEventRepository.deleteAll()
  }

  @Test
  fun `should return NOT_STARTED when no DTR exists`() {
    restTestClient.get().uri("/cases/{crn}/dtr", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedNotStartedDtrResponseBody(case.id, crn))
      }
  }

  @Test
  fun `should return DTR with submission and localAuthorityAreaName`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()

    val existingEntity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = case.id,
        localAuthorityAreaId = localAuthorityArea.id,
        referenceNumber = "DTR-REF-001",
        submissionDate = LocalDate.of(2026, 1, 15),
        status = EntityDtrStatus.SUBMITTED,
      ),
    )

    restTestClient.get().uri("/cases/{crn}/dtr", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedDtrResponseBody(
            id = existingEntity.id,
            caseId = case.id,
            crn = crn,
            localAuthorityAreaId = localAuthorityArea.id,
            localAuthorityAreaName = localAuthorityArea.name,
            submissionDate = "2026-01-15",
            referenceNumber = "DTR-REF-001",
            createdBy = NAME_OF_TEST_DATA_SETUP_USER,
            createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          ),
        )
      }
  }

  @Test
  fun `should get duty to refer by id with ADDA role`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()

    val entity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = case.id,
        localAuthorityAreaId = localAuthorityArea.id,
        referenceNumber = "DTR-REF-001",
        submissionDate = LocalDate.of(2026, 1, 15),
        status = EntityDtrStatus.SUBMITTED,
      ),
    )

    restTestClient.get().uri("/duty-to-refers/{id}", entity.id)
      .withClientCredentialsJwt(
        roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE__ACCOMMODATION_DATA_DOMAIN"),
      )
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedDtrResponseBody(
            id = entity.id,
            caseId = case.id,
            crn = crn,
            localAuthorityAreaId = localAuthorityArea.id,
            localAuthorityAreaName = localAuthorityArea.name,
            createdBy = NAME_OF_TEST_DATA_SETUP_USER,
            createdAt = entity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          ),
        )
      }
  }

  @Test
  fun `should return 404 when duty to refer not found for ADDA role`() {
    val nonExistentId = UUID.randomUUID()

    restTestClient.get().uri("/duty-to-refers/{id}", nonExistentId)
      .withDeliusUserJwt(roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE__ACCOMMODATION_DATA_DOMAIN"))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 403 when using token with SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER for DTR callback endpoint`() {
    val nonExistentId = UUID.randomUUID()

    restTestClient.get().uri("/duty-to-refers/{id}", nonExistentId)
      .withDeliusUserJwt(roles = listOf("SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER"))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should create duty to refer and publish domain event`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()

    val result = restTestClient.post().uri("/cases/$crn/dtr")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createDtrRequestBody(
          localAuthorityAreaId = localAuthorityArea.id,
          submissionDate = "2026-01-15",
          referenceNumber = "DTR-REF-001",
          status = "SUBMITTED",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .returnResult().responseBody!!

    val persistedRecord = dutyToReferRepository.findByCaseId(case.id)!!
    assertPersistedDutyToRefer(persistedRecord, localAuthorityArea.id)

    assertThatJson(result).matchesExpectedJson(
      expectedDtrResponseBody(
        id = persistedRecord.id,
        caseId = case.id,
        crn = crn,
        localAuthorityAreaId = localAuthorityArea.id,
        localAuthorityAreaName = localAuthorityArea.name,
        submissionDate = "2026-01-15",
        referenceNumber = "DTR-REF-001",
        createdBy = NAME_OF_LOGGED_IN_DELIUS_USER,
        createdAt = persistedRecord.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
      ),
    )

    assertPublishedSNSEvent(
      dutyToReferId = persistedRecord.id,
      eventType = SingleAccommodationServiceDomainEventType.SAS_DUTY_TO_REFER_UPDATED,
      eventDescription = SingleAccommodationServiceDomainEventType.SAS_DUTY_TO_REFER_UPDATED.typeDescription,
    )
    assertThatOutboxIsAsExpected(persistedRecord.id)
  }

  @Test
  fun `should update duty to refer and return 200 with updated data`() {
    val localAuthorityAreaId = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first().id
    val newLocalAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().last()

    val existingEntity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = case.id,
        localAuthorityAreaId = localAuthorityAreaId,
        referenceNumber = "DTR-REF-001",
        submissionDate = LocalDate.of(2026, 1, 15),
        status = EntityDtrStatus.SUBMITTED,
      ),
    )

    val result = restTestClient.put().uri("/cases/$crn/dtr/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createDtrRequestBody(
          localAuthorityAreaId = newLocalAuthorityArea.id,
          submissionDate = LocalDate.of(2026, 1, 20).toString(),
          referenceNumber = "DTR-REF-002",
          status = EntityDtrStatus.ACCEPTED.name,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .returnResult().responseBody!!

    assertThatJson(result).matchesExpectedJson(
      expectedDtrResponseBody(
        id = existingEntity.id,
        caseId = case.id,
        crn = crn,
        localAuthorityAreaId = newLocalAuthorityArea.id,
        localAuthorityAreaName = newLocalAuthorityArea.name,
        submissionDate = LocalDate.of(2026, 1, 20).toString(),
        referenceNumber = "DTR-REF-002",
        status = DtrStatus.ACCEPTED.name,
        createdBy = NAME_OF_TEST_DATA_SETUP_USER,
        createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
      ),
    )

    assertPublishedSNSEvent(
      dutyToReferId = existingEntity.id,
      eventType = SingleAccommodationServiceDomainEventType.SAS_DUTY_TO_REFER_UPDATED,
      eventDescription = SingleAccommodationServiceDomainEventType.SAS_DUTY_TO_REFER_UPDATED.typeDescription,
    )
    assertThatOutboxIsAsExpected(existingEntity.id)
  }

  @Test
  fun `should return 404 when updating nonexistent duty to refer`() {
    val nonExistentId = UUID.randomUUID()
    val localAuthorityAreaId = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first().id

    restTestClient.put().uri("/cases/$crn/dtr/$nonExistentId")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createDtrRequestBody(
          localAuthorityAreaId = localAuthorityAreaId,
          status = EntityDtrStatus.SUBMITTED.name,
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 404 when updating DTR with CRN that does not match duty to refer`() {
    val localAuthorityAreaId = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first().id
    val existingEntity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = case.id,
        localAuthorityAreaId = localAuthorityAreaId,
        status = EntityDtrStatus.SUBMITTED,
      ),
    )

    restTestClient.put().uri("/cases/OTHERCRN/dtr/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createDtrRequestBody(
          localAuthorityAreaId = localAuthorityAreaId,
          status = EntityDtrStatus.ACCEPTED.name,
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should update duty to refer and not publish domain event when status stays the same`() {
    val localAuthorityAreaId = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first().id
    val existingEntity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = case.id,
        localAuthorityAreaId = localAuthorityAreaId,
        status = EntityDtrStatus.SUBMITTED,
      ),
    )

    restTestClient.put().uri("/cases/$crn/dtr/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createDtrRequestBody(
          localAuthorityAreaId = localAuthorityAreaId,
          submissionDate = "2026-01-20",
          referenceNumber = "DTR-REF-001",
          status = EntityDtrStatus.SUBMITTED.name,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    assertThat(outboxEventRepository.findAll()).isEmpty()
  }

  private fun assertPersistedDutyToRefer(
    persistedRecord: DutyToReferEntity,
    localAuthorityAreaId: UUID,
  ) {
    assertThat(persistedRecord.caseId).isEqualTo(case.id)
    assertThat(persistedRecord.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
    assertThat(persistedRecord.referenceNumber).isEqualTo("DTR-REF-001")
    assertThat(persistedRecord.submissionDate).isEqualTo(LocalDate.of(2026, 1, 15))
    assertThat(persistedRecord.status).isEqualTo(EntityDtrStatus.SUBMITTED)
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
    assertThat(outboxRecord.domainEventType).isEqualTo(SingleAccommodationServiceDomainEventType.SAS_DUTY_TO_REFER_UPDATED.name)
    assertThatJson(outboxRecord.payload).matchesExpectedJson(expectedDutyToReferUpdatedDomainEventJson(dutyToReferId))
    assertThat(outboxRecord.processedStatus).isEqualTo(ProcessedStatus.SUCCESS)
  }
}
