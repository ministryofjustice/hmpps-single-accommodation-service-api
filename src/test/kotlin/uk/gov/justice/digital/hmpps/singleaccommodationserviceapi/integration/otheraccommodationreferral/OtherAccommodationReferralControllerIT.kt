package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.expectBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OtherAccommodationReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.createOtherAccommodationReferralRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.expectedOtherAccommodationReferralResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.otherAccommodationReferralNoteRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

class OtherAccommodationReferralControllerIT : IntegrationTestBase() {

  @Autowired
  private lateinit var otherAccommodationReferralRepository: OtherAccommodationReferralRepository

  @Autowired
  private lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  private lateinit var crn: String
  private lateinit var case: CaseEntity

  private lateinit var beforeTest: Instant

  @BeforeEach
  fun setup() {
    beforeTest = Instant.now()
    databaseUtils.truncate(
      SasTables.OTHER_ACCOMMODATION_REFERRAL,
    )
    case = caseRepository.save(buildCaseEntity())
    crn = case.caseIdentifiers.first().identifier
    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()
  }

  @Test
  fun `should create other accommodation referral and return it in the response`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()

    val result = restTestClient.post().uri("/cases/$crn/other-accommodation-referral")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOtherAccommodationReferralRequestBody(
          localAuthorityAreaId = localAuthorityArea.id,
          submissionDate = "2026-02-20",
          referenceNumber = "REF-001",
          status = "SUBMITTED",
          organisationName = "Organisation name",
          website = "https://www.charity.org",
          submissionNote = "A submission note",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    val persistedRecord = otherAccommodationReferralRepository.findByCaseId(case.id)!!
    assertPersistedOtherAccommodationReferral(persistedRecord, localAuthorityArea.id)

    assertThatJson(result).matchesExpectedJson(
      expectedOtherAccommodationReferralResponseBody(
        id = persistedRecord.id,
        caseId = case.id,
        crn = crn,
        localAuthorityAreaId = localAuthorityArea.id,
        localAuthorityAreaName = localAuthorityArea.name,
        submissionDate = "2026-02-20",
        referenceNumber = "REF-001",
        status = "SUBMITTED",
        createdBy = NAME_OF_LOGGED_IN_DELIUS_USER,
        createdByUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
        createdAt = persistedRecord.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        organisationName = "Organisation name",
        website = "https://www.charity.org",
        submissionNote = "A submission note",
      ),
    )
  }

  @Test
  fun `should create other accommodation referral with only mandatory fields`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()

    val result = restTestClient.post().uri("/cases/$crn/other-accommodation-referral")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        createOtherAccommodationReferralRequestBody(
          localAuthorityAreaId = localAuthorityArea.id,
          referenceNumber = null,
          organisationName = null,
          website = null,
          submissionNote = null,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    val persistedRecord = otherAccommodationReferralRepository.findByCaseId(case.id)!!
    assertThat(persistedRecord.referenceNumber).isNull()
    assertThat(persistedRecord.organisationName).isNull()
    assertThat(persistedRecord.website).isNull()
    assertThat(persistedRecord.submissionNote).isNull()

    assertThatJson(result).matchesExpectedJson(
      expectedOtherAccommodationReferralResponseBody(
        id = persistedRecord.id,
        caseId = case.id,
        crn = crn,
        localAuthorityAreaId = localAuthorityArea.id,
        localAuthorityAreaName = localAuthorityArea.name,
        referenceNumber = null,
        organisationName = null,
        website = null,
        submissionNote = null,
        createdBy = NAME_OF_LOGGED_IN_DELIUS_USER,
        createdByUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
        createdAt = persistedRecord.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
      ),
    )
  }

  @Test
  fun `should return 404 when case does not exist for crn`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()

    restTestClient.post().uri("/cases/{crn}/other-accommodation-referral", "NONEXISTENT")
      .contentType(MediaType.APPLICATION_JSON)
      .body(createOtherAccommodationReferralRequestBody(localAuthorityAreaId = localAuthorityArea.id))
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 404 when local authority does not exist`() {
    restTestClient.post().uri("/cases/$crn/other-accommodation-referral")
      .contentType(MediaType.APPLICATION_JSON)
      .body(createOtherAccommodationReferralRequestBody(localAuthorityAreaId = UUID.randomUUID()))
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 403 when user does not have the required role`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()

    restTestClient.post().uri("/cases/$crn/other-accommodation-referral")
      .contentType(MediaType.APPLICATION_JSON)
      .body(createOtherAccommodationReferralRequestBody(localAuthorityAreaId = localAuthorityArea.id))
      .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should create a note for other accommodation referral`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()
    val existingEntity = createOtherAccommodationReferral(localAuthorityArea.id)
    val note1Value = "Test note 1"
    val note2Value = "Test note 2"

    restTestClient.post().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(otherAccommodationReferralNoteRequestBody(note = note1Value))
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    var persistedResult = otherAccommodationReferralRepository.findByIdAndCrnWithNotes(existingEntity.id, crn)!!
    assertThat(persistedResult.notes.first().note).isEqualTo(note1Value)

    restTestClient.post().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(otherAccommodationReferralNoteRequestBody(note = note2Value))
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    persistedResult = otherAccommodationReferralRepository.findByIdAndCrnWithNotes(existingEntity.id, crn)!!
    val sortedNotes = persistedResult.notes.sortedByDescending { it.createdAt }
    assertThat(sortedNotes.first().note).isEqualTo(note2Value)
    assertThat(sortedNotes[1].note).isEqualTo(note1Value)
  }

  @Test
  fun `should not create a note when other accommodation referral not found`() {
    restTestClient.post().uri("/cases/$crn/other-accommodation-referral/${UUID.randomUUID()}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(otherAccommodationReferralNoteRequestBody(note = "Test note"))
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should not create a note when crn not found`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()
    val existingEntity = createOtherAccommodationReferral(localAuthorityArea.id)

    restTestClient.post().uri("/cases/${UUID.randomUUID()}/other-accommodation-referral/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(otherAccommodationReferralNoteRequestBody(note = "Test note"))
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should fail with Bad Request for empty note`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()
    val existingEntity = createOtherAccommodationReferral(localAuthorityArea.id)

    restTestClient.post().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(otherAccommodationReferralNoteRequestBody(note = ""))
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should fail with Bad Request for note exceeding 4000 characters`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()
    val existingEntity = createOtherAccommodationReferral(localAuthorityArea.id)

    restTestClient.post().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(otherAccommodationReferralNoteRequestBody(note = "a".repeat(4001)))
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should return 403 for createNote when user does not have the required role`() {
    val localAuthorityArea = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()
    val existingEntity = createOtherAccommodationReferral(localAuthorityArea.id)

    restTestClient.post().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(otherAccommodationReferralNoteRequestBody(note = "Test note"))
      .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
      .exchange()
      .expectStatus().isForbidden
  }

  private fun createOtherAccommodationReferral(localAuthorityAreaId: UUID) = otherAccommodationReferralRepository.save(
    buildOtherAccommodationReferralEntity(
      crn = crn,
      caseId = case.id,
      localAuthorityAreaId = localAuthorityAreaId,
    ),
  )

  private fun assertPersistedOtherAccommodationReferral(
    persistedRecord: OtherAccommodationReferralEntity,
    localAuthorityAreaId: UUID,
  ) {
    assertThat(persistedRecord.crn).isEqualTo(crn)
    assertThat(persistedRecord.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
    assertThat(persistedRecord.referenceNumber).isEqualTo("REF-001")
    assertThat(persistedRecord.submissionDate).isEqualTo(LocalDate.of(2026, 2, 20))
    assertThat(persistedRecord.status).isEqualTo(OtherAccommodationReferralStatus.SUBMITTED)
    assertThat(persistedRecord.organisationName).isEqualTo("Organisation name")
    assertThat(persistedRecord.website).isEqualTo("https://www.charity.org")
    assertThat(persistedRecord.submissionNote).isEqualTo("A submission note")
    assertThat(persistedRecord.createdAt).isBetween(
      beforeTest.minusSeconds(1),
      Instant.now().plusSeconds(1),
    )
  }
}
