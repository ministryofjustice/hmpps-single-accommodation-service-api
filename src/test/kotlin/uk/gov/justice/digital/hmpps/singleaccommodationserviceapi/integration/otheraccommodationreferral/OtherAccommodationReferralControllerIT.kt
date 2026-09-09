package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.javers.core.Javers
import org.javers.repository.jql.QueryBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.expectBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OtherAccommodationReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_TEST_DATA_SETUP_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.createOorRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.expectedGetOorResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.expectedGetOtherAccommodationReferralTimelineResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.expectedOorResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.oorNoteRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus as EntityOorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutcomeReason as EntityOutcomeReason

class OtherAccommodationReferralControllerIT : IntegrationTestBase() {

  @Autowired
  private lateinit var otherAccommodationReferralRepository: OtherAccommodationReferralRepository

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
    databaseUtils.truncate(
      SasTables.INBOX_EVENT,
      SasTables.OUTBOX_EVENT,
      SasTables.OTHER_ACCOMMODATION_REFERRAL,
    )
    case = caseRepository.save(buildCaseEntity())
    crn = case.caseIdentifiers.first().identifier
    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()
  }

  @Test
  fun `should get other accommodation referral by id with ADDA role`() {
    val entity = otherAccommodationReferralRepository.save(
      buildOtherAccommodationReferralEntity(
        caseId = case.id,
        referenceNumber = "OOR-REF-001",
        submissionDate = LocalDate.of(2026, 1, 15),
        status = EntityOorStatus.SUBMITTED,
      ),
    )

    restTestClient.get().uri("/other-accommodation-referrals/{id}", entity.id)
      .withClientCredentialsJwt(
        roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE__ACCOMMODATION_DATA_DOMAIN"),
      )
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetOorResponseBody(
            id = entity.id,
            caseId = case.id,
            crn = crn,
            createdBy = NAME_OF_TEST_DATA_SETUP_USER,
            createdAt = entity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          ),
        )
      }
  }

  @Test
  fun `should return 404 when other accommodation referral not found for ADDA role`() {
    val nonExistentId = UUID.randomUUID()

    restTestClient.get().uri("/other-accommodation-referrals/{id}", nonExistentId)
      .withDeliusUserJwt(roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE__ACCOMMODATION_DATA_DOMAIN"))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should get other accommodation referral by crn and id`() {
    val entity = otherAccommodationReferralRepository.save(
      buildOtherAccommodationReferralEntity(
        caseId = case.id,
        referenceNumber = "OOR-REF-001",
        submissionDate = LocalDate.now().minusMonths(5),
        status = EntityOorStatus.SUBMITTED,
      ),
    )

    restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}", crn, entity.id)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetOorResponseBody(
            id = entity.id,
            caseId = case.id,
            crn = crn,
            submissionDate = entity.submissionDate.toString(),
            referenceNumber = "OOR-REF-001",
            createdBy = NAME_OF_TEST_DATA_SETUP_USER,
            createdAt = entity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
            active = true,
          ),
        )
      }
  }

  @Test
  fun `should return active false for an expired referral by crn and id`() {
    val entity = otherAccommodationReferralRepository.save(
      buildOtherAccommodationReferralEntity(
        caseId = case.id,
        referenceNumber = "OOR-REF-001",
        submissionDate = LocalDate.now().minusMonths(7),
        status = EntityOorStatus.SUBMITTED,
      ),
    )

    restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}", crn, entity.id)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetOorResponseBody(
            id = entity.id,
            caseId = case.id,
            crn = crn,
            submissionDate = entity.submissionDate.toString(),
            referenceNumber = "OOR-REF-001",
            createdBy = NAME_OF_TEST_DATA_SETUP_USER,
            createdAt = entity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
            active = false,
          ),
        )
      }
  }

  @Test
  fun `should return 404 when other accommodation referral not found by crn and id`() {
    val nonExistentId = UUID.randomUUID()

    restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}", crn, nonExistentId)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 404 when crn does not match for GET by crn and id`() {
    val entity = createOor()

    restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}", "OTHERCRN", entity.id)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should create other accommodation referral`() {
    val result = restTestClient.post().uri("/cases/$crn/other-accommodation-referral")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          submissionDate = "2026-01-15",
          referenceNumber = "OOR-REF-001",
          status = "SUBMITTED",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    val persistedRecord = otherAccommodationReferralRepository.findByCaseId(case.id)!!
    assertPersistedOtherAccommodationReferral(persistedRecord)

    assertThatJson(result).matchesExpectedJson(
      expectedOorResponseBody(
        id = persistedRecord.id,
        caseId = case.id,
        crn = crn,
        submissionDate = "2026-01-15",
        referenceNumber = "OOR-REF-001",
        createdBy = NAME_OF_LOGGED_IN_DELIUS_USER,
        createdAt = persistedRecord.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
      ),
    )
  }

  @Test
  fun `should update other accommodation referral and return 200 with updated data`() {
    val existingEntity = otherAccommodationReferralRepository.save(
      buildOtherAccommodationReferralEntity(
        caseId = case.id,
        referenceNumber = "OOR-REF-001",
        submissionDate = LocalDate.of(2026, 1, 15),
        status = EntityOorStatus.SUBMITTED,
      ),
    )

    val result = restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          submissionDate = LocalDate.of(2026, 1, 20).toString(),
          referenceNumber = "OOR-REF-002",
          status = EntityOorStatus.ACCEPTED.name,
          outcomeReason = "PRIORITY_NEED",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    assertThatJson(result).matchesExpectedJson(
      expectedOorResponseBody(
        id = existingEntity.id,
        caseId = case.id,
        crn = crn,
        submissionDate = LocalDate.of(2026, 1, 20).toString(),
        referenceNumber = "OOR-REF-002",
        status = OorStatus.ACCEPTED.name,
        createdBy = NAME_OF_TEST_DATA_SETUP_USER,
        createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        outcomeReason = "PRIORITY_NEED",
      ),
    )
  }

  @Test
  fun `should return 404 when updating nonexistent other accommodation referral`() {
    val nonExistentId = UUID.randomUUID()

    restTestClient.put().uri("/cases/$crn/other-accommodation-referral/$nonExistentId")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          status = EntityOorStatus.SUBMITTED.name,
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 404 when updating other accommodation referral with CRN that does not match other accommodation referral`() {
    val existingEntity = createOor()

    restTestClient.put().uri("/cases/OTHERCRN/other-accommodation-referral/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          status = EntityOorStatus.ACCEPTED.name,
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should reject OOR referral with an outcome reason and return 200`() {
    val existingEntity = createOor()

    val result = restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          status = EntityOorStatus.NOT_ACCEPTED.name,
          outcomeReason = "NO_LOCAL_CONNECTION",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    assertThatJson(result).matchesExpectedJson(
      expectedOorResponseBody(
        id = existingEntity.id,
        caseId = case.id,
        crn = crn,
        status = OorStatus.NOT_ACCEPTED.name,
        createdBy = NAME_OF_TEST_DATA_SETUP_USER,
        createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        outcomeReason = "NO_LOCAL_CONNECTION",
      ),
    )
  }

  @Test
  fun `should return 400 when accepting OOR referral without an outcome reason`() {
    val existingEntity = createOor()

    restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          status = EntityOorStatus.ACCEPTED.name,
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should return 400 when accepting OOR referral with a NOT_ACCEPTED outcome reason`() {
    val existingEntity = createOor()

    restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          status = EntityOorStatus.ACCEPTED.name,
          outcomeReason = "NO_LOCAL_CONNECTION",
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should return 400 when providing an outcome reason for a SUBMITTED status`() {
    val existingEntity = createOor()

    restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          status = EntityOorStatus.SUBMITTED.name,
          outcomeReason = "PRIORITY_NEED",
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should update other accommodation referral when status stays the same`() {
    val existingEntity = createOor()

    restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          submissionDate = "2026-01-20",
          referenceNumber = "OOR-REF-001",
          status = EntityOorStatus.SUBMITTED.name,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    val updatedRecord = otherAccommodationReferralRepository.findById(existingEntity.id).get()
    assertThat(updatedRecord.submissionDate).isEqualTo(LocalDate.of(2026, 1, 20))
    assertThat(updatedRecord.referenceNumber).isEqualTo("OOR-REF-001")
    assertThat(updatedRecord.status).isEqualTo(EntityOorStatus.SUBMITTED)
  }

  @Test
  fun `should create a note for oor`() {
    val existingEntity = createOor()
    val note1Value = "Test note 1"
    val note2Value = "Test note 2"
    restTestClient.post().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        oorNoteRequestBody(
          note = note1Value,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    var oorPersistedResult = otherAccommodationReferralRepository.findByIdAndCrnWithNotes(existingEntity.id, crn)!!
    assertThat(oorPersistedResult.notes.first().note).isEqualTo(note1Value)

    restTestClient.post().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        oorNoteRequestBody(
          note = note2Value,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    oorPersistedResult = otherAccommodationReferralRepository.findByIdAndCrnWithNotes(existingEntity.id, crn)!!
    val sortedNotes: List<OtherAccommodationReferralNoteEntity> = oorPersistedResult.notes.sortedByDescending { it.createdAt }
    assertThat(sortedNotes.first().note).isEqualTo(note2Value)
    assertThat(sortedNotes[1].note).isEqualTo(note1Value)
  }

  @Test
  fun `should not create a note for oor when oor not found`() {
    restTestClient.post().uri("/cases/$crn/other-accommodation-referral/${UUID.randomUUID()}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        oorNoteRequestBody(
          note = "Test note",
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should not create a note when crn not found`() {
    val existingEntity = createOor()
    restTestClient.post().uri("/cases/${UUID.randomUUID()}/oor/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        oorNoteRequestBody(
          note = "Test note",
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should fail with Bad Request for empty note`() {
    val existingEntity = createOor()
    val note = ""
    restTestClient.post().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        oorNoteRequestBody(note),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should fail with Bad Request for note exceeding 4000 characters`() {
    val existingEntity = createOor()
    val note = "a".repeat(4001)
    restTestClient.post().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        oorNoteRequestBody(note),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should return OOR timeline when a OOR is created`() {
    val createdOor = restTestClient.post().uri("/cases/{crn}/other-accommodation-referral", crn)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          submissionDate = "2026-01-15",
          referenceNumber = "OOR-REF-001",
          status = "SUBMITTED",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    val createdOorId = ObjectMapper().readTree(createdOor).get("submission").get("id").asText()
    val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdOorId))
    assertThat(commitTimesAsc).hasSize(1)

    restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}/timeline", crn, createdOorId)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetOtherAccommodationReferralTimelineResponse(
            otherAccommodationReferralId = UUID.fromString(createdOorId),
            caseId = case.id,
            createCommitTime = commitTimesAsc.first().truncatedTo(ChronoUnit.SECONDS).toString(),
          ),
        )
      }
  }

  @Test
  fun `should return OOR timeline when it is created, a note is added, and it is updated twice`() {
    val createdOor = restTestClient.post().uri("/cases/{crn}/other-accommodation-referral", crn)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          submissionDate = "2026-01-15",
          referenceNumber = "OOR-REF-001",
          status = "SUBMITTED",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    val createdOorId = ObjectMapper().readTree(createdOor).get("submission").get("id").asText()

    restTestClient.post().uri("/cases/{crn}/other-accommodation-referral/{id}/notes", crn, createdOorId)
      .contentType(MediaType.APPLICATION_JSON)
      .body(oorNoteRequestBody(note = "Test note"))
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    restTestClient.put().uri("/cases/{crn}/other-accommodation-referral/{id}", crn, createdOorId)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          submissionDate = "2026-01-15",
          referenceNumber = "OOR-REF-002",
          status = EntityOorStatus.NOT_ACCEPTED.name,
          outcomeReason = "NO_LOCAL_CONNECTION",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    restTestClient.put().uri("/cases/{crn}/other-accommodation-referral/{id}", crn, createdOorId)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          submissionDate = "2026-01-15",
          referenceNumber = "OOR-REF-002",
          status = EntityOorStatus.ACCEPTED.name,
          outcomeReason = "PRIORITY_NEED",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdOorId))
    assertThat(commitTimesAsc).hasSize(3)
    val createNoteCommitTime = otherAccommodationReferralRepository.findByIdAndCrnWithNotes(UUID.fromString(createdOorId), crn)!!
      .notes.first().createdAt

    restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}/timeline", crn, createdOorId)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetOtherAccommodationReferralTimelineResponse(
            otherAccommodationReferralId = UUID.fromString(createdOorId),
            caseId = case.id,
            createCommitTime = commitTimesAsc.first().truncatedTo(ChronoUnit.SECONDS).toString(),
            createNoteCommitTime = createNoteCommitTime!!.truncatedTo(ChronoUnit.SECONDS).toString(),
            update1CommitTime = commitTimesAsc[1].truncatedTo(ChronoUnit.SECONDS).toString(),
            update2CommitTime = commitTimesAsc[2].truncatedTo(ChronoUnit.SECONDS).toString(),
          ),
        )
      }
  }

  @Test
  fun `should return 404 for timeline when OOR not found`() {
    restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}/timeline", crn, UUID.randomUUID())
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 404 for timeline when crn does not match`() {
    val entity = createOor()

    restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}/timeline", "OTHERCRN", entity.id)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  private fun getCommitTimesAsc(otherAccommodationReferralId: UUID): List<Instant> {
    val changes = javers.findChanges(
      QueryBuilder.byInstanceId(otherAccommodationReferralId, OtherAccommodationReferralEntity::class.java).build(),
    )
    return changes.groupBy { it.commitMetadata.get().id }.entries
      .map { (_, commitChanges) -> commitChanges.first().commitMetadata.get().commitDateInstant }
      .sorted()
  }

  private fun assertPersistedOtherAccommodationReferral(
    persistedRecord: OtherAccommodationReferralEntity,
  ) {
    assertThat(persistedRecord.caseId).isEqualTo(case.id)
    assertThat(persistedRecord.referenceNumber).isEqualTo("OOR-REF-001")
    assertThat(persistedRecord.submissionDate).isEqualTo(LocalDate.of(2026, 1, 15))
    assertThat(persistedRecord.status).isEqualTo(EntityOorStatus.SUBMITTED)
    assertThat(persistedRecord.createdByUserId).isEqualTo(userIdOfLoggedInDeliusUser)
    assertThat(persistedRecord.createdAt).isBetween(
      beforeTest.minusSeconds(1),
      Instant.now().plusSeconds(1),
    )
  }

  @Test
  fun `should create other accommodation referral with submission note and return it in the response`() {
    val noteText = "This is a submission note"

    val result = restTestClient.post().uri("/cases/$crn/other-accommodation-referral")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          submissionDate = "2026-01-15",
          referenceNumber = "OOR-REF-001",
          status = "SUBMITTED",
          submissionNote = noteText,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    val persistedRecord = otherAccommodationReferralRepository.findByCaseId(case.id)!!
    assertThat(persistedRecord.submissionNote).isEqualTo(noteText)
    assertThat(persistedRecord.outcomeNote).isNull()

    assertThatJson(result).matchesExpectedJson(
      expectedOorResponseBody(
        id = persistedRecord.id,
        caseId = case.id,
        crn = crn,
        submissionDate = "2026-01-15",
        referenceNumber = "OOR-REF-001",
        createdBy = NAME_OF_LOGGED_IN_DELIUS_USER,
        createdAt = persistedRecord.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        submissionNote = noteText,
      ),
    )
  }

  @Test
  fun `should create other accommodation referral with blank note and not persist the note`() {
    restTestClient.post().uri("/cases/$crn/other-accommodation-referral")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          status = "SUBMITTED",
          submissionNote = "   ",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    val persistedRecord = otherAccommodationReferralRepository.findByCaseId(case.id)!!
    assertThat(persistedRecord.submissionNote).isNull()
  }

  @Test
  fun `should update other accommodation referral with both submission and outcome notes and return them in the response`() {
    val submissionNoteText = "Original submission note"
    val outcomeNoteText = "This is an outcome note"

    val existingEntity = otherAccommodationReferralRepository.save(
      buildOtherAccommodationReferralEntity(
        caseId = case.id,
        status = EntityOorStatus.SUBMITTED,
        submissionNote = submissionNoteText,
      ),
    )

    val result = restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          status = EntityOorStatus.ACCEPTED.name,
          outcomeReason = "PRIORITY_NEED",
          submissionNote = submissionNoteText,
          outcomeNote = outcomeNoteText,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    val updatedRecord = otherAccommodationReferralRepository.findByCaseId(case.id)!!
    assertThat(updatedRecord.submissionNote).isEqualTo(submissionNoteText)
    assertThat(updatedRecord.outcomeNote).isEqualTo(outcomeNoteText)

    assertThatJson(result).matchesExpectedJson(
      expectedOorResponseBody(
        id = existingEntity.id,
        caseId = case.id,
        crn = crn,
        status = "ACCEPTED",
        createdBy = NAME_OF_TEST_DATA_SETUP_USER,
        createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        outcomeReason = "PRIORITY_NEED",
        submissionNote = submissionNoteText,
        outcomeNote = outcomeNoteText,
      ),
    )
  }

  @Test
  fun `should update other accommodation referral with blank note and not overwrite existing outcome note`() {
    val existingEntity = createOor()

    restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          status = EntityOorStatus.ACCEPTED.name,
          outcomeReason = "PRIORITY_NEED",
          outcomeNote = "  ",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    val updatedRecord = otherAccommodationReferralRepository.findByCaseId(case.id)!!
    assertThat(updatedRecord.outcomeNote).isNull()
  }

  @Test
  fun `should update submission note on an existing SUBMITTED other accommodation referral`() {
    val originalNote = "Original submission note"
    val updatedNote = "Updated submission note"

    val existingEntity = otherAccommodationReferralRepository.save(
      buildOtherAccommodationReferralEntity(
        caseId = case.id,
        status = EntityOorStatus.SUBMITTED,
        submissionNote = originalNote,
      ),
    )

    val result = restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          status = EntityOorStatus.SUBMITTED.name,
          submissionNote = updatedNote,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    val updatedRecord = otherAccommodationReferralRepository.findByCaseId(case.id)!!
    assertThat(updatedRecord.submissionNote).isEqualTo(updatedNote)

    assertThatJson(result).matchesExpectedJson(
      expectedOorResponseBody(
        id = existingEntity.id,
        caseId = case.id,
        crn = crn,
        createdBy = NAME_OF_TEST_DATA_SETUP_USER,
        createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        submissionNote = updatedNote,
      ),
    )
  }

  @Test
  fun `should update submission note on a other accommodation referral that already has an outcome`() {
    val outcomeNoteText = "This is an outcome note"
    val updatedNote = "Updated submission note"

    val existingEntity = otherAccommodationReferralRepository.save(
      buildOtherAccommodationReferralEntity(
        caseId = case.id,
        status = EntityOorStatus.ACCEPTED,
        outcomeReason = EntityOutcomeReason.PREVENTION_AND_RELIEF_DUTY,
        submissionNote = "Original submission note",
        outcomeNote = outcomeNoteText,
      ),
    )

    restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOorRequestBody(
          status = EntityOorStatus.ACCEPTED.name,
          outcomeReason = "PREVENTION_AND_RELIEF_DUTY",
          submissionNote = updatedNote,
          outcomeNote = outcomeNoteText,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    val updatedRecord = otherAccommodationReferralRepository.findByCaseId(case.id)!!
    assertThat(updatedRecord.submissionNote).isEqualTo(updatedNote)
    assertThat(updatedRecord.outcomeNote).isEqualTo(outcomeNoteText)
  }

  private fun createOor(): OtherAccommodationReferralEntity {
    val existingEntity = otherAccommodationReferralRepository.save(
      buildOtherAccommodationReferralEntity(
        caseId = case.id,
        status = EntityOorStatus.SUBMITTED,
      ),
    )
    return existingEntity
  }
}
