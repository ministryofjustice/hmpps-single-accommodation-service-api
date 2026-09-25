package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.javers.core.Javers
import org.javers.repository.jql.QueryBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.expectBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralOutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditOverrideContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OtherAccommodationReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_TEST_DATA_SETUP_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_TEST_DATA_SETUP_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.createOtherAccommodationReferralRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.expectedGetOtherAccommodationReferralOutcomeTimelineResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.expectedGetOtherAccommodationReferralResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.expectedGetOtherAccommodationReferralTimelineResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.expectedOtherAccommodationReferralResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.expectedSearchOtherAccommodationReferralResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json.otherAccommodationReferralNoteRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralOutcomeReason as EntityOtherAccommodationReferralOutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralStatus as EntityOtherAccommodationReferralStatus

class OtherAccommodationReferralControllerIT : IntegrationTestBase() {

  @Autowired
  private lateinit var otherAccommodationReferralRepository: OtherAccommodationReferralRepository

  @Autowired
  private lateinit var javers: Javers

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

  @Nested
  inner class CreateOtherAccommodationReferral {
    @Test
    fun `should create other accommodation referral and return it in the response`() {
      val result = restTestClient.post().uri("/cases/$crn/other-accommodation-referral")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
            submissionDate = "2026-02-20",
            referenceNumber = "REF-001",
            status = EntityOtherAccommodationReferralStatus.SUBMITTED.name,
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
      assertPersistedOtherAccommodationReferral(persistedRecord)

      assertThatJson(result).matchesExpectedJson(
        expectedOtherAccommodationReferralResponseBody(
          id = persistedRecord.id,
          caseId = case.id,
          crn = crn,
          submissionDate = "2026-02-20",
          referenceNumber = "REF-001",
          status = OtherAccommodationReferralStatus.SUBMITTED.name,
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
      val result = restTestClient.post().uri("/cases/$crn/other-accommodation-referral")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
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
      restTestClient.post().uri("/cases/{crn}/other-accommodation-referral", "NONEXISTENT")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createOtherAccommodationReferralRequestBody())
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 403 when user does not have the required role`() {
      restTestClient.post().uri("/cases/$crn/other-accommodation-referral")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createOtherAccommodationReferralRequestBody())
        .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
        .exchange()
        .expectStatus().isForbidden
    }

    private fun assertPersistedOtherAccommodationReferral(
      persistedRecord: OtherAccommodationReferralEntity,
    ) {
      assertThat(persistedRecord.crn).isEqualTo(crn)
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

  @Nested
  inner class GetOtherAccommodationReferral {
    @Test
    fun `should get other accommodation referral by crn and id`() {
      val entity = otherAccommodationReferralRepository.save(
        buildOtherAccommodationReferralEntity(
          caseId = case.id,
          crn = crn,
          referenceNumber = "REF-001",
          submissionDate = LocalDate.of(2026, 2, 20),
          organisationName = "Organisation name",
          website = "https://www.charity.org",
          submissionNote = "A submission note",
          status = OtherAccommodationReferralStatus.SUBMITTED,
        ),
      )

      val result = restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}", crn, entity.id)
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      assertThatJson(result).matchesExpectedJson(
        expectedGetOtherAccommodationReferralResponseBody(
          id = entity.id,
          caseId = case.id,
          crn = crn,
          submissionDate = "2026-02-20",
          referenceNumber = "REF-001",
          status = OtherAccommodationReferralStatus.SUBMITTED.name,
          createdBy = NAME_OF_TEST_DATA_SETUP_USER,
          createdByUsername = USERNAME_OF_TEST_DATA_SETUP_USER,
          createdAt = entity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          organisationName = "Organisation name",
          website = "https://www.charity.org",
          submissionNote = "A submission note",
        ),
      )
    }

    @Test
    fun `should get other accommodation referral with only mandatory fields`() {
      val entity = otherAccommodationReferralRepository.save(
        buildOtherAccommodationReferralEntity(
          caseId = case.id,
          crn = crn,
          referenceNumber = null,
          submissionDate = LocalDate.of(2026, 2, 20),
          organisationName = null,
          website = null,
          submissionNote = null,
          status = OtherAccommodationReferralStatus.SUBMITTED,
        ),
      )

      val result = restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}", crn, entity.id)
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      assertThatJson(result).matchesExpectedJson(
        expectedGetOtherAccommodationReferralResponseBody(
          id = entity.id,
          caseId = case.id,
          crn = crn,
          submissionDate = "2026-02-20",
          referenceNumber = null,
          status = OtherAccommodationReferralStatus.SUBMITTED.name,
          createdBy = NAME_OF_TEST_DATA_SETUP_USER,
          createdByUsername = USERNAME_OF_TEST_DATA_SETUP_USER,
          createdAt = entity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          organisationName = null,
          website = null,
          submissionNote = null,
        ),
      )
    }

    @Test
    fun `should return 404 on get when other accommodation referral not found by id`() {
      val nonExistentId = UUID.randomUUID()

      restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}", crn, nonExistentId)
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 404 on get when crn does not match`() {
      val entity = otherAccommodationReferralRepository.save(
        buildOtherAccommodationReferralEntity(
          caseId = case.id,
          crn = crn,
        ),
      )

      restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}", "OTHERCRN", entity.id)
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 403 on get when user does not have required role`() {
      val entity = otherAccommodationReferralRepository.save(
        buildOtherAccommodationReferralEntity(
          caseId = case.id,
          crn = crn,
        ),
      )

      restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}", crn, entity.id)
        .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Nested
  inner class SearchOtherAccommodationReferrals {
    @Test
    fun `should return referrals in descending order of submission date`() {
      val oldest = otherAccommodationReferralRepository.save(
        buildOtherAccommodationReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityOtherAccommodationReferralStatus.SUBMITTED,
          submissionDate = LocalDate.of(2026, 6, 17),
          createdAt = Instant.parse("2026-07-22T00:00:00Z"),
        ),
      )
      val newest = otherAccommodationReferralRepository.save(
        buildOtherAccommodationReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityOtherAccommodationReferralStatus.ACCEPTED,
          submissionDate = LocalDate.of(2026, 7, 19),
          createdAt = Instant.parse("2026-07-22T00:00:00Z"),
        ),
      )

      val result = searchOtherAccommodationReferrals(statuses = listOf("SUBMITTED", "ACCEPTED"))

      assertThatJson(result).matchesExpectedJson(
        expectedSearchOtherAccommodationReferralResponseBody(
          listOf(
            expectedResponseBodyFor(newest),
            expectedResponseBodyFor(oldest),
          ),
        ),
      )
    }

    @Test
    fun `should filter referrals by a single status`() {
      val submitted = otherAccommodationReferralRepository.save(
        buildOtherAccommodationReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityOtherAccommodationReferralStatus.SUBMITTED,
        ),
      )

      otherAccommodationReferralRepository.save(
        buildOtherAccommodationReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityOtherAccommodationReferralStatus.REJECTED,
        ),
      )

      val result = searchOtherAccommodationReferrals(statuses = listOf("SUBMITTED"))

      assertThatJson(result).matchesExpectedJson(
        expectedSearchOtherAccommodationReferralResponseBody(
          listOf(expectedResponseBodyFor(submitted)),
        ),
      )
    }

    @Test
    fun `should filter referrals by multiple statuses`() {
      val submitted = otherAccommodationReferralRepository.save(
        buildOtherAccommodationReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityOtherAccommodationReferralStatus.SUBMITTED,
          submissionDate = LocalDate.of(2026, 1, 10),
        ),
      )
      val accepted = otherAccommodationReferralRepository.save(
        buildOtherAccommodationReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityOtherAccommodationReferralStatus.ACCEPTED,
          submissionDate = LocalDate.of(2026, 1, 20),
        ),
      )
      otherAccommodationReferralRepository.save(
        buildOtherAccommodationReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityOtherAccommodationReferralStatus.REJECTED,
        ),
      )

      val result = searchOtherAccommodationReferrals(statuses = listOf("SUBMITTED", "ACCEPTED"))

      assertThatJson(result).matchesExpectedJson(
        expectedSearchOtherAccommodationReferralResponseBody(
          listOf(
            // most recently submitted first
            expectedResponseBodyFor(accepted),
            expectedResponseBodyFor(submitted),
          ),
        ),
      )
    }

    @Test
    fun `should return the correct created by user details for each referral`() {
      val referralByTestDataSetupUser = otherAccommodationReferralRepository.save(
        buildOtherAccommodationReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityOtherAccommodationReferralStatus.SUBMITTED,
          submissionDate = LocalDate.of(2026, 1, 1),
        ),
      )
      val referralByDeliusUser = AuditOverrideContext.withAuditorId(userIdOfLoggedInDeliusUser) {
        otherAccommodationReferralRepository.save(
          buildOtherAccommodationReferralEntity(
            caseId = case.id,
            crn = crn,
            status = EntityOtherAccommodationReferralStatus.ACCEPTED,
            submissionDate = LocalDate.of(2026, 2, 1),
          ),
        )
      }

      val result = searchOtherAccommodationReferrals(statuses = listOf("SUBMITTED", "ACCEPTED"))

      assertThatJson(result).matchesExpectedJson(
        expectedSearchOtherAccommodationReferralResponseBody(
          listOf(
            expectedResponseBodyFor(
              referralByDeliusUser,
              createdBy = NAME_OF_LOGGED_IN_DELIUS_USER,
              createdByUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
            ),
            expectedResponseBodyFor(referralByTestDataSetupUser),
          ),
        ),
      )
    }

    @Test
    fun `should return empty list when no referrals exist for the crn`() {
      val result = searchOtherAccommodationReferrals()

      assertThatJson(result).matchesExpectedJson(
        expectedSearchOtherAccommodationReferralResponseBody(emptyList()),
      )
    }

    @Test
    fun `should return populated outcomeReason and outcomeNote for an accepted referral`() {
      val accepted = otherAccommodationReferralRepository.save(
        buildOtherAccommodationReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityOtherAccommodationReferralStatus.ACCEPTED,
          outcomeReason = EntityOtherAccommodationReferralOutcomeReason.ACCEPTED_BY_ORGANISATION,
          outcomeNote = "An outcome note",
        ),
      )

      val result = searchOtherAccommodationReferrals(statuses = listOf("ACCEPTED"))

      assertThatJson(result).matchesExpectedJson(
        expectedSearchOtherAccommodationReferralResponseBody(
          listOf(expectedResponseBodyFor(accepted)),
        ),
      )
    }

    @Test
    fun `should return 403 on search when user does not have required role`() {
      restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/search", crn)
        .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
        .exchange()
        .expectStatus().isForbidden
    }

    private fun searchOtherAccommodationReferrals(statuses: List<String>? = null): String = restTestClient.get().uri {
      it.path("/cases/$crn/other-accommodation-referral/search")
      if (statuses != null) {
        it.queryParam("statuses", *statuses.toTypedArray())
      }
      it.build()
    }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    private fun expectedResponseBodyFor(
      entity: OtherAccommodationReferralEntity,
      createdBy: String = NAME_OF_TEST_DATA_SETUP_USER,
      createdByUsername: String = USERNAME_OF_TEST_DATA_SETUP_USER,
    ): String = expectedOtherAccommodationReferralResponseBody(
      id = entity.id,
      caseId = case.id,
      crn = crn,
      submissionDate = entity.submissionDate.toString(),
      referenceNumber = entity.referenceNumber,
      status = entity.status.name,
      createdBy = createdBy,
      createdByUsername = createdByUsername,
      createdAt = entity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
      organisationName = entity.organisationName,
      website = entity.website,
      submissionNote = entity.submissionNote,
      outcomeReason = entity.outcomeReason?.name,
      outcomeNote = entity.outcomeNote,
    )
  }

  @Nested
  inner class UpdateOtherAccommodationReferral {
    @Test
    fun `should update other accommodation referral and return 200 with updated data`() {
      val existingEntity = createOtherAccommodationReferralEntity(
        referenceNumber = "REF-001",
        organisationName = "Organisation name",
        website = "https://www.charity.org",
        submissionNote = "A submission note",
      )

      val result = restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
            submissionDate = "2026-01-20",
            referenceNumber = "REF-002",
            organisationName = "New organisation name",
            website = "https://www.new-charity.org",
            submissionNote = "An updated submission note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      val updatedRecord = otherAccommodationReferralRepository.findByCaseId(case.id)!!
      assertThat(updatedRecord.referenceNumber).isEqualTo("REF-002")
      assertThat(updatedRecord.submissionDate).isEqualTo(LocalDate.of(2026, 1, 20))
      assertThat(updatedRecord.organisationName).isEqualTo("New organisation name")
      assertThat(updatedRecord.website).isEqualTo("https://www.new-charity.org")
      assertThat(updatedRecord.submissionNote).isEqualTo("An updated submission note")

      assertThatJson(result).matchesExpectedJson(
        expectedOtherAccommodationReferralResponseBody(
          id = existingEntity.id,
          caseId = case.id,
          crn = crn,
          submissionDate = "2026-01-20",
          referenceNumber = "REF-002",
          createdBy = NAME_OF_TEST_DATA_SETUP_USER,
          createdByUsername = USERNAME_OF_TEST_DATA_SETUP_USER,
          createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          organisationName = "New organisation name",
          website = "https://www.new-charity.org",
          submissionNote = "An updated submission note",
        ),
      )
    }

    @Test
    fun `should return 404 when updating nonexistent other accommodation referral`() {
      val nonExistentId = UUID.randomUUID()

      restTestClient.put().uri("/cases/$crn/other-accommodation-referral/$nonExistentId")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createOtherAccommodationReferralRequestBody())
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 404 when updating other accommodation referral with CRN that does not match`() {
      val existingEntity = createOtherAccommodationReferralEntity()

      restTestClient.put().uri("/cases/OTHERCRN/other-accommodation-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createOtherAccommodationReferralRequestBody())
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 403 when user does not have the required role for update`() {
      val existingEntity = createOtherAccommodationReferralEntity()

      restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createOtherAccommodationReferralRequestBody())
        .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should accept other accommodation referral with an outcome reason and note`() {
      val existingEntity = createOtherAccommodationReferralEntity()

      val result = restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
            status = EntityOtherAccommodationReferralStatus.ACCEPTED.name,
            outcomeReason = EntityOtherAccommodationReferralOutcomeReason.ACCEPTED_WITH_ACCOMMODATION_PLACEMENT.name,
            outcomeNote = "An outcome note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      assertThatJson(result).matchesExpectedJson(
        expectedOtherAccommodationReferralResponseBody(
          id = existingEntity.id,
          caseId = case.id,
          crn = crn,
          status = OtherAccommodationReferralStatus.ACCEPTED.name,
          createdBy = NAME_OF_TEST_DATA_SETUP_USER,
          createdByUsername = USERNAME_OF_TEST_DATA_SETUP_USER,
          createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          outcomeReason = OtherAccommodationReferralOutcomeReason.ACCEPTED_WITH_ACCOMMODATION_PLACEMENT.name,
          outcomeNote = "An outcome note",
        ),
      )
    }

    @Test
    fun `should reject other accommodation referral with an outcome reason and note`() {
      val existingEntity = createOtherAccommodationReferralEntity()

      val result = restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
            status = EntityOtherAccommodationReferralStatus.REJECTED.name,
            outcomeReason = EntityOtherAccommodationReferralOutcomeReason.NO_CAPACITY.name,
            outcomeNote = "An outcome note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      assertThatJson(result).matchesExpectedJson(
        expectedOtherAccommodationReferralResponseBody(
          id = existingEntity.id,
          caseId = case.id,
          crn = crn,
          status = OtherAccommodationReferralStatus.REJECTED.name,
          createdBy = NAME_OF_TEST_DATA_SETUP_USER,
          createdByUsername = USERNAME_OF_TEST_DATA_SETUP_USER,
          createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          outcomeReason = OtherAccommodationReferralOutcomeReason.NO_CAPACITY.name,
          outcomeNote = "An outcome note",
        ),
      )
    }

    @Test
    fun `should return 400 when accepting other accommodation referral without an outcome reason`() {
      val existingEntity = createOtherAccommodationReferralEntity()

      restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
            status = EntityOtherAccommodationReferralStatus.ACCEPTED.name,
          ),
        )
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `should return 400 when accepting other accommodation referral with a REJECTED-only outcome reason`() {
      val existingEntity = createOtherAccommodationReferralEntity()

      restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
            status = EntityOtherAccommodationReferralStatus.ACCEPTED.name,
            outcomeReason = EntityOtherAccommodationReferralOutcomeReason.NO_CAPACITY.name,
          ),
        )
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `should return 400 when providing an outcome reason for a SUBMITTED status`() {
      val existingEntity = createOtherAccommodationReferralEntity()

      restTestClient.put().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
            status = EntityOtherAccommodationReferralStatus.SUBMITTED.name,
            outcomeReason = EntityOtherAccommodationReferralOutcomeReason.ACCEPTED_BY_ORGANISATION.name,
          ),
        )
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isBadRequest
    }
  }

  @Nested
  inner class CreateOtherAccommodationReferralNote {
    @Test
    fun `should create a note for other accommodation referral`() {
      val existingEntity = createOtherAccommodationReferralEntity()
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
      val existingEntity = createOtherAccommodationReferralEntity()

      restTestClient.post().uri("/cases/${UUID.randomUUID()}/other-accommodation-referral/${existingEntity.id}/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .body(otherAccommodationReferralNoteRequestBody(note = "Test note"))
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should fail with Bad Request for empty note`() {
      val existingEntity = createOtherAccommodationReferralEntity()

      restTestClient.post().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .body(otherAccommodationReferralNoteRequestBody(note = ""))
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `should fail with Bad Request for note exceeding 4000 characters`() {
      val existingEntity = createOtherAccommodationReferralEntity()

      restTestClient.post().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .body(otherAccommodationReferralNoteRequestBody(note = "a".repeat(4001)))
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `should return 403 for createNote when user does not have the required role`() {
      val existingEntity = createOtherAccommodationReferralEntity()

      restTestClient.post().uri("/cases/$crn/other-accommodation-referral/${existingEntity.id}/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .body(otherAccommodationReferralNoteRequestBody(note = "Test note"))
        .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Nested
  inner class GetOtherAccommodationReferralTimeline {
    @Test
    fun `should return other accommodation referral timeline when an other accommodation referral is created`() {
      val createdOtherAccommodationReferral = restTestClient.post().uri("/cases/{crn}/other-accommodation-referral", crn)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
            submissionDate = "2026-01-15",
            referenceNumber = "REF-001",
            submissionNote = "A submission note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      val createdOtherAccommodationReferralId = ObjectMapper().readTree(createdOtherAccommodationReferral).get("submission").get("id").asText()
      val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdOtherAccommodationReferralId))
      assertThat(commitTimesAsc).hasSize(1)

      restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}/timeline", crn, createdOtherAccommodationReferralId)
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .value {
          assertThatJson(it!!).matchesExpectedJson(
            expectedGetOtherAccommodationReferralTimelineResponse(
              otherAccommodationReferralId = UUID.fromString(createdOtherAccommodationReferralId),
              caseId = case.id,
              crn = crn,
              createCommitTime = commitTimesAsc.first().truncatedTo(ChronoUnit.SECONDS).toString(),
            ),
          )
        }
    }

    @Test
    fun `should return other accommodation referral timeline with notes and updates`() {
      val createdOtherAccommodationReferral = restTestClient.post().uri("/cases/{crn}/other-accommodation-referral", crn)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
            submissionDate = "2026-01-15",
            referenceNumber = "REF-001",
            submissionNote = "A submission note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      val createdOtherAccommodationReferralId = ObjectMapper().readTree(createdOtherAccommodationReferral).get("submission").get("id").asText()

      restTestClient.post().uri("/cases/{crn}/other-accommodation-referral/{id}/notes", crn, createdOtherAccommodationReferralId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(otherAccommodationReferralNoteRequestBody(note = "Test note"))
        .withDeliusUserJwt()
        .exchangeSuccessfully()

      restTestClient.put().uri("/cases/{crn}/other-accommodation-referral/{id}", crn, createdOtherAccommodationReferralId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
            submissionDate = "2026-01-15",
            referenceNumber = "REF-002",
            submissionNote = "A submission note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()

      val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdOtherAccommodationReferralId))
      assertThat(commitTimesAsc).hasSize(2)
      val createNoteCommitTime = otherAccommodationReferralRepository.findByIdAndCrnWithNotes(UUID.fromString(createdOtherAccommodationReferralId), crn)!!
        .notes.first().createdAt

      restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}/timeline", crn, createdOtherAccommodationReferralId)
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .value {
          assertThatJson(it!!).matchesExpectedJson(
            expectedGetOtherAccommodationReferralTimelineResponse(
              otherAccommodationReferralId = UUID.fromString(createdOtherAccommodationReferralId),
              caseId = case.id,
              crn = crn,
              referenceNumber = "REF-002",
              previousReferenceNumber = "REF-001",
              createCommitTime = commitTimesAsc.first().truncatedTo(ChronoUnit.SECONDS).toString(),
              createNoteCommitTime = createNoteCommitTime!!.truncatedTo(ChronoUnit.SECONDS).toString(),
              updateCommitTime = commitTimesAsc[1].truncatedTo(ChronoUnit.SECONDS).toString(),
            ),
          )
        }
    }

    @Test
    fun `should return other accommodation referral timeline when it is accepted with an outcome reason and note`() {
      val createdOtherAccommodationReferral = restTestClient.post().uri("/cases/{crn}/other-accommodation-referral", crn)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
            submissionDate = "2026-01-15",
            referenceNumber = "REF-001",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      val createdOtherAccommodationReferralId = ObjectMapper().readTree(createdOtherAccommodationReferral).get("submission").get("id").asText()

      restTestClient.put().uri("/cases/{crn}/other-accommodation-referral/{id}", crn, createdOtherAccommodationReferralId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
            submissionDate = "2026-01-15",
            referenceNumber = "REF-001",
            status = EntityOtherAccommodationReferralStatus.ACCEPTED.name,
            outcomeReason = EntityOtherAccommodationReferralOutcomeReason.ACCEPTED_WITH_ACCOMMODATION_PLACEMENT.name,
            outcomeNote = "An outcome note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()

      val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdOtherAccommodationReferralId))
      assertThat(commitTimesAsc).hasSize(2)

      restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}/timeline", crn, createdOtherAccommodationReferralId)
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .value {
          assertThatJson(it!!).matchesExpectedJson(
            expectedGetOtherAccommodationReferralOutcomeTimelineResponse(
              otherAccommodationReferralId = UUID.fromString(createdOtherAccommodationReferralId),
              caseId = case.id,
              crn = crn,
              createCommitTime = commitTimesAsc.first().truncatedTo(ChronoUnit.SECONDS).toString(),
              updateCommitTime = commitTimesAsc[1].truncatedTo(ChronoUnit.SECONDS).toString(),
              newStatus = OtherAccommodationReferralStatus.ACCEPTED.name,
              outcomeReason = OtherAccommodationReferralOutcomeReason.ACCEPTED_WITH_ACCOMMODATION_PLACEMENT.name,
              outcomeNote = "An outcome note",
            ),
          )
        }
    }

    @Test
    fun `should return other accommodation referral timeline when it is rejected with an outcome reason and note`() {
      val createdOtherAccommodationReferral = restTestClient.post().uri("/cases/{crn}/other-accommodation-referral", crn)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
            submissionDate = "2026-01-15",
            referenceNumber = "REF-001",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      val createdOtherAccommodationReferralId = ObjectMapper().readTree(createdOtherAccommodationReferral).get("submission").get("id").asText()

      restTestClient.put().uri("/cases/{crn}/other-accommodation-referral/{id}", crn, createdOtherAccommodationReferralId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createOtherAccommodationReferralRequestBody(
            submissionDate = "2026-01-15",
            referenceNumber = "REF-001",
            status = EntityOtherAccommodationReferralStatus.REJECTED.name,
            outcomeReason = EntityOtherAccommodationReferralOutcomeReason.NO_CAPACITY.name,
            outcomeNote = "Another outcome note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()

      val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdOtherAccommodationReferralId))
      assertThat(commitTimesAsc).hasSize(2)

      restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}/timeline", crn, createdOtherAccommodationReferralId)
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .value {
          assertThatJson(it!!).matchesExpectedJson(
            expectedGetOtherAccommodationReferralOutcomeTimelineResponse(
              otherAccommodationReferralId = UUID.fromString(createdOtherAccommodationReferralId),
              caseId = case.id,
              crn = crn,
              createCommitTime = commitTimesAsc.first().truncatedTo(ChronoUnit.SECONDS).toString(),
              updateCommitTime = commitTimesAsc[1].truncatedTo(ChronoUnit.SECONDS).toString(),
              newStatus = OtherAccommodationReferralStatus.REJECTED.name,
              outcomeReason = OtherAccommodationReferralOutcomeReason.NO_CAPACITY.name,
              outcomeNote = "Another outcome note",
            ),
          )
        }
    }

    @Test
    fun `should return 404 for timeline when other accommodation referral not found`() {
      restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}/timeline", crn, UUID.randomUUID())
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 404 for timeline when crn does not match`() {
      val entity = createOtherAccommodationReferralEntity()

      restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}/timeline", "OTHERCRN", entity.id)
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 403 for timeline when user does not have the required role`() {
      val entity = createOtherAccommodationReferralEntity()

      restTestClient.get().uri("/cases/{crn}/other-accommodation-referral/{id}/timeline", crn, entity.id)
        .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
        .exchange()
        .expectStatus().isForbidden
    }

    private fun getCommitTimesAsc(otherAccommodationReferralId: UUID): List<Instant> {
      val changes = javers.findChanges(
        QueryBuilder.byInstanceId(otherAccommodationReferralId, OtherAccommodationReferralEntity::class.java).build(),
      )
      return changes.groupBy { it.commitMetadata.get().id }.entries
        .map { (_, commitChanges) -> commitChanges.first().commitMetadata.get().commitDateInstant }
        .sorted()
    }
  }

  private fun createOtherAccommodationReferralEntity(
    referenceNumber: String? = "OA-REF-001",
    submissionDate: LocalDate = LocalDate.of(2026, 1, 15),
    status: OtherAccommodationReferralStatus = OtherAccommodationReferralStatus.SUBMITTED,
    organisationName: String? = null,
    website: String? = null,
    submissionNote: String? = null,
  ): OtherAccommodationReferralEntity = otherAccommodationReferralRepository.save(
    buildOtherAccommodationReferralEntity(
      crn = crn,
      caseId = case.id,
      referenceNumber = referenceNumber,
      submissionDate = submissionDate,
      status = status,
      organisationName = organisationName,
      website = website,
      submissionNote = submissionNote,
    ),
  )
}
