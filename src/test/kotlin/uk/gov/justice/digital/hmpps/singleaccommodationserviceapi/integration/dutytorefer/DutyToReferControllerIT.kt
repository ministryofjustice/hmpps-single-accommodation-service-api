package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.javers.core.Javers
import org.javers.repository.jql.QueryBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildNomisUserDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_TEST_DATA_SETUP_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_NOMIS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json.createDtrRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json.dtrNoteRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json.expectedDtrResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json.expectedDutyToReferUpdatedDomainEventJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json.expectedGetDutyToReferTimelineResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json.expectedGetDtrResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json.expectedNotStartedDtrResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.NomisUserRolesStubs
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

  @Autowired
  private lateinit var javers: Javers

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
          expectedGetDtrResponseBody(
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
          expectedGetDtrResponseBody(
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
  fun `should get duty to refer by crn and id`() {
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

    restTestClient.get().uri("/cases/{crn}/dtr/{id}", crn, entity.id)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetDtrResponseBody(
            id = entity.id,
            caseId = case.id,
            crn = crn,
            localAuthorityAreaId = localAuthorityArea.id,
            localAuthorityAreaName = localAuthorityArea.name,
            submissionDate = "2026-01-15",
            referenceNumber = "DTR-REF-001",
            createdBy = NAME_OF_TEST_DATA_SETUP_USER,
            createdAt = entity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          ),
        )
      }
  }

  @Test
  fun `should return 404 when duty to refer not found by crn and id`() {
    val nonExistentId = UUID.randomUUID()

    restTestClient.get().uri("/cases/{crn}/dtr/{id}", crn, nonExistentId)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 404 when crn does not match for GET by crn and id`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()

    val entity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = case.id,
        localAuthorityAreaId = localAuthorityArea.id,
        status = EntityDtrStatus.SUBMITTED,
      ),
    )

    restTestClient.get().uri("/cases/{crn}/dtr/{id}", "OTHERCRN", entity.id)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
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

  @Test
  fun `should create a note for dtr`() {
    val localAuthorityAreaId = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first().id
    val existingEntity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = case.id,
        localAuthorityAreaId = localAuthorityAreaId,
        status = EntityDtrStatus.SUBMITTED,
      ),
    )
    val note1Value = "Test note 1"
    val note2Value = "Test note 2"
    restTestClient.post().uri("/cases/$crn/dtr/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        dtrNoteRequestBody(
          note = note1Value,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    var dtrPersistedResult = dutyToReferRepository.findByIdAndCrnWithNotes(existingEntity.id, crn)!!
    assertThat(dtrPersistedResult.notes.first().note).isEqualTo(note1Value)

    restTestClient.post().uri("/cases/$crn/dtr/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        dtrNoteRequestBody(
          note = note2Value,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    dtrPersistedResult = dutyToReferRepository.findByIdAndCrnWithNotes(existingEntity.id, crn)!!
    val sortedNotes: List<DutyToReferNoteEntity> = dtrPersistedResult.notes.sortedByDescending { it.createdAt }
    assertThat(sortedNotes.first().note).isEqualTo(note2Value)
    assertThat(sortedNotes[1].note).isEqualTo(note1Value)
  }

  @Test
  fun `should not create a note for dtr when dtr not found`() {
    restTestClient.post().uri("/cases/$crn/dtr/${UUID.randomUUID()}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        dtrNoteRequestBody(
          note = "Test note",
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should not create a note when crn not found`() {
    val localAuthorityAreaId = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first().id
    val existingEntity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = case.id,
        localAuthorityAreaId = localAuthorityAreaId,
        status = EntityDtrStatus.SUBMITTED,
      ),
    )
    restTestClient.post().uri("/cases/${UUID.randomUUID()}/dtr/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        dtrNoteRequestBody(
          note = "Test note",
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should fail with Bad Request for empty note`() {
    val localAuthorityAreaId = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first().id
    val existingEntity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = case.id,
        localAuthorityAreaId = localAuthorityAreaId,
        status = EntityDtrStatus.SUBMITTED,
      ),
    )
    val note = ""
    restTestClient.post().uri("/cases/$crn/dtr/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        dtrNoteRequestBody(note),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should fail with Bad Request for note exceeding 4000 characters`() {
    val localAuthorityAreaId = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first().id
    val existingEntity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = case.id,
        localAuthorityAreaId = localAuthorityAreaId,
        status = EntityDtrStatus.SUBMITTED,
      ),
    )
    val note = "a".repeat(4001)
    restTestClient.post().uri("/cases/$crn/dtr/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        dtrNoteRequestBody(note),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should return DTR timeline when a DTR is created`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()

    val createdDtr = restTestClient.post().uri("/cases/{crn}/dtr", crn)
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

    val createdDtrId = ObjectMapper().readTree(createdDtr).get("submission").get("id").asText()
    val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdDtrId))
    assertThat(commitTimesAsc).hasSize(1)

    restTestClient.get().uri("/cases/{crn}/dtr/{id}/timeline", crn, createdDtrId)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetDutyToReferTimelineResponse(
            dutyToReferId = UUID.fromString(createdDtrId),
            caseId = case.id,
            localAuthorityAreaId = localAuthorityArea.id,
            localAuthorityAreaName = localAuthorityArea.name,
            createCommitTime = commitTimesAsc.first().truncatedTo(ChronoUnit.SECONDS).toString(),
          ),
        )
      }
  }

  @Test
  fun `should return DTR timeline when it is created, a note is added, and it is updated twice`() {
    val initialLocalAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()
    val updatedLocalAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().last()

    val createdDtr = restTestClient.post().uri("/cases/{crn}/dtr", crn)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createDtrRequestBody(
          localAuthorityAreaId = initialLocalAuthorityArea.id,
          submissionDate = "2026-01-15",
          referenceNumber = "DTR-REF-001",
          status = "SUBMITTED",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .returnResult().responseBody!!

    val createdDtrId = ObjectMapper().readTree(createdDtr).get("submission").get("id").asText()

    restTestClient.post().uri("/cases/{crn}/dtr/{id}/notes", crn, createdDtrId)
      .contentType(MediaType.APPLICATION_JSON)
      .body(dtrNoteRequestBody(note = "Test note"))
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    NomisUserRolesStubs.stubMe(
      jwt = jwtAuthHelper.createJwtAccessToken(
        USERNAME_OF_LOGGED_IN_NOMIS_USER,
        roles = listOf("ROLE_POM", "ROLE_PRISON"),
        authSource = AuthSource.NOMIS.source,
      ),
      response = buildNomisUserDetail(
        USERNAME_OF_LOGGED_IN_NOMIS_USER,
        primaryEmail = USERNAME_OF_LOGGED_IN_NOMIS_USER,
      ),
    )

    restTestClient.put().uri("/cases/{crn}/dtr/{id}", crn, createdDtrId)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createDtrRequestBody(
          localAuthorityAreaId = initialLocalAuthorityArea.id,
          submissionDate = "2026-01-15",
          referenceNumber = "DTR-REF-002",
          status = EntityDtrStatus.NOT_ACCEPTED.name,
        ),
      )
      .withNomisUserJwt()
      .exchangeSuccessfully()

    restTestClient.put().uri("/cases/{crn}/dtr/{id}", crn, createdDtrId)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createDtrRequestBody(
          localAuthorityAreaId = updatedLocalAuthorityArea.id,
          submissionDate = "2026-01-15",
          referenceNumber = "DTR-REF-002",
          status = EntityDtrStatus.ACCEPTED.name,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdDtrId))
    assertThat(commitTimesAsc).hasSize(3)
    val createNoteCommitTime = dutyToReferRepository.findByIdAndCrnWithNotes(UUID.fromString(createdDtrId), crn)!!
      .notes.first().createdAt

    restTestClient.get().uri("/cases/{crn}/dtr/{id}/timeline", crn, createdDtrId)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetDutyToReferTimelineResponse(
            dutyToReferId = UUID.fromString(createdDtrId),
            caseId = case.id,
            initialLocalAuthorityAreaId = initialLocalAuthorityArea.id,
            initialLocalAuthorityAreaName = initialLocalAuthorityArea.name,
            updatedLocalAuthorityAreaId = updatedLocalAuthorityArea.id,
            updatedLocalAuthorityAreaName = updatedLocalAuthorityArea.name,
            createCommitTime = commitTimesAsc.first().truncatedTo(ChronoUnit.SECONDS).toString(),
            createNoteCommitTime = createNoteCommitTime!!.truncatedTo(ChronoUnit.SECONDS).toString(),
            update1CommitTime = commitTimesAsc[1].truncatedTo(ChronoUnit.SECONDS).toString(),
            update2CommitTime = commitTimesAsc[2].truncatedTo(ChronoUnit.SECONDS).toString(),
          ),
        )
      }
  }

  @Test
  fun `should return 404 for timeline when DTR not found`() {
    restTestClient.get().uri("/cases/{crn}/dtr/{id}/timeline", crn, UUID.randomUUID())
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 404 for timeline when crn does not match`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()
    val entity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = case.id,
        localAuthorityAreaId = localAuthorityArea.id,
        status = EntityDtrStatus.SUBMITTED,
      ),
    )

    restTestClient.get().uri("/cases/{crn}/dtr/{id}/timeline", "OTHERCRN", entity.id)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  private fun getCommitTimesAsc(dutyToReferId: UUID): List<Instant> {
    val changes = javers.findChanges(
      QueryBuilder.byInstanceId(dutyToReferId, DutyToReferEntity::class.java).build(),
    )
    return changes.groupBy { it.commitMetadata.get().id }.entries
      .map { (_, commitChanges) -> commitChanges.first().commitMetadata.get().commitDateInstant }
      .sorted()
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
    assertThat(outboxRecord.processedStatus).isEqualTo(ProcessedStatus.PROCESSED)
  }
}
