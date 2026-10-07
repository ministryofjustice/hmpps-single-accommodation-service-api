package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.externalreferral

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralWithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditOverrideContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildExternalReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ExternalReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_TEST_DATA_SETUP_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_TEST_DATA_SETUP_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.externalreferral.json.createExternalReferralRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.externalreferral.json.expectedExternalReferralResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.externalreferral.json.expectedGetExternalReferralOutcomeTimelineResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.externalreferral.json.expectedGetExternalReferralResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.externalreferral.json.expectedGetExternalReferralTimelineResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.externalreferral.json.expectedSearchExternalReferralResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.externalreferral.json.externalReferralNoteRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralStatus as EntityExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralWithdrawalReason as EntityExternalReferralWithdrawalReason

class ExternalReferralControllerIT : IntegrationTestBase() {

  @Autowired
  private lateinit var externalReferralRepository: ExternalReferralRepository

  @Autowired
  private lateinit var javers: Javers

  private lateinit var crn: String
  private lateinit var case: CaseEntity

  private lateinit var beforeTest: Instant

  @BeforeEach
  fun setup() {
    beforeTest = Instant.now()
    databaseUtils.truncate(
      SasTables.EXTERNAL_REFERRAL,
    )
    case = caseRepository.save(buildCaseEntity())
    crn = case.caseIdentifiers.first().identifier
    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()
  }

  @Nested
  inner class CreateExternalReferral {
    @Test
    fun `should create external referral and return it in the response`() {
      val result = restTestClient.post().uri("/cases/$crn/external-referral")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
            submissionDate = "2026-02-20",
            referenceNumber = "REF-001",
            status = ExternalReferralStatus.SUBMITTED.name,
            organisationName = "Organisation name",
            website = "https://www.charity.org",
            submissionNote = "A submission note",
            email = "contact@example.com",
            phoneNumber = "01234567890",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      val persistedRecord = externalReferralRepository.findByCaseId(case.id)!!
      assertPersistedExternalReferral(persistedRecord)

      assertThatJson(result).matchesExpectedJson(
        expectedExternalReferralResponseBody(
          id = persistedRecord.id,
          caseId = case.id,
          crn = crn,
          submissionDate = "2026-02-20",
          referenceNumber = "REF-001",
          status = ExternalReferralStatus.SUBMITTED.name,
          createdBy = NAME_OF_LOGGED_IN_DELIUS_USER,
          createdByUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
          createdAt = persistedRecord.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          organisationName = "Organisation name",
          website = "https://www.charity.org",
          submissionNote = "A submission note",
          email = "contact@example.com",
          phoneNumber = "01234567890",
        ),
      )
    }

    @Test
    fun `should create external referral with only mandatory fields`() {
      val result = restTestClient.post().uri("/cases/$crn/external-referral")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
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

      val persistedRecord = externalReferralRepository.findByCaseId(case.id)!!
      assertThat(persistedRecord.referenceNumber).isNull()
      assertThat(persistedRecord.organisationName).isNull()
      assertThat(persistedRecord.website).isNull()
      assertThat(persistedRecord.submissionNote).isNull()

      assertThatJson(result).matchesExpectedJson(
        expectedExternalReferralResponseBody(
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
      restTestClient.post().uri("/cases/{crn}/external-referral", "NONEXISTENT")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createExternalReferralRequestBody())
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 403 when user does not have the required role`() {
      restTestClient.post().uri("/cases/$crn/external-referral")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createExternalReferralRequestBody())
        .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
        .exchange()
        .expectStatus().isForbidden
    }

    private fun assertPersistedExternalReferral(
      persistedRecord: ExternalReferralEntity,
    ) {
      assertThat(persistedRecord.crn).isEqualTo(crn)
      assertThat(persistedRecord.referenceNumber).isEqualTo("REF-001")
      assertThat(persistedRecord.submissionDate).isEqualTo(LocalDate.of(2026, 2, 20))
      assertThat(persistedRecord.status).isEqualTo(EntityExternalReferralStatus.SUBMITTED)
      assertThat(persistedRecord.organisationName).isEqualTo("Organisation name")
      assertThat(persistedRecord.website).isEqualTo("https://www.charity.org")
      assertThat(persistedRecord.submissionNote).isEqualTo("A submission note")
      assertThat(persistedRecord.email).isEqualTo("contact@example.com")
      assertThat(persistedRecord.phoneNumber).isEqualTo("01234567890")
      assertThat(persistedRecord.createdAt).isBetween(
        beforeTest.minusSeconds(1),
        Instant.now().plusSeconds(1),
      )
    }
  }

  @Nested
  inner class GetExternalReferral {
    @Test
    fun `should get external referral by crn and id`() {
      val entity = externalReferralRepository.save(
        buildExternalReferralEntity(
          caseId = case.id,
          crn = crn,
          referenceNumber = "REF-001",
          submissionDate = LocalDate.of(2026, 2, 20),
          organisationName = "Organisation name",
          website = "https://www.charity.org",
          submissionNote = "A submission note",
          status = EntityExternalReferralStatus.SUBMITTED,
        ),
      )

      val result = restTestClient.get().uri("/cases/{crn}/external-referral/{id}", crn, entity.id)
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      assertThatJson(result).matchesExpectedJson(
        expectedGetExternalReferralResponseBody(
          id = entity.id,
          caseId = case.id,
          crn = crn,
          submissionDate = "2026-02-20",
          referenceNumber = "REF-001",
          status = ExternalReferralStatus.SUBMITTED.name,
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
    fun `should get external referral with only mandatory fields`() {
      val entity = externalReferralRepository.save(
        buildExternalReferralEntity(
          caseId = case.id,
          crn = crn,
          referenceNumber = null,
          submissionDate = LocalDate.of(2026, 2, 20),
          organisationName = null,
          website = null,
          submissionNote = null,
          status = EntityExternalReferralStatus.SUBMITTED,
        ),
      )

      val result = restTestClient.get().uri("/cases/{crn}/external-referral/{id}", crn, entity.id)
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      assertThatJson(result).matchesExpectedJson(
        expectedGetExternalReferralResponseBody(
          id = entity.id,
          caseId = case.id,
          crn = crn,
          submissionDate = "2026-02-20",
          referenceNumber = null,
          status = ExternalReferralStatus.SUBMITTED.name,
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
    fun `should return 404 on get when external referral not found by id`() {
      val nonExistentId = UUID.randomUUID()

      restTestClient.get().uri("/cases/{crn}/external-referral/{id}", crn, nonExistentId)
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 404 on get when crn does not match`() {
      val entity = externalReferralRepository.save(
        buildExternalReferralEntity(
          caseId = case.id,
          crn = crn,
        ),
      )

      restTestClient.get().uri("/cases/{crn}/external-referral/{id}", "OTHERCRN", entity.id)
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 403 on get when user does not have required role`() {
      val entity = externalReferralRepository.save(
        buildExternalReferralEntity(
          caseId = case.id,
          crn = crn,
        ),
      )

      restTestClient.get().uri("/cases/{crn}/external-referral/{id}", crn, entity.id)
        .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Nested
  inner class SearchExternalReferrals {
    @Test
    fun `should return referrals in descending order of submission date`() {
      val oldest = externalReferralRepository.save(
        buildExternalReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityExternalReferralStatus.SUBMITTED,
          submissionDate = LocalDate.of(2026, 6, 17),
          createdAt = Instant.parse("2026-07-22T00:00:00Z"),
        ),
      )
      val newest = externalReferralRepository.save(
        buildExternalReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityExternalReferralStatus.ACCEPTED,
          submissionDate = LocalDate.of(2026, 7, 19),
          createdAt = Instant.parse("2026-07-22T00:00:00Z"),
        ),
      )

      val result = searchExternalReferrals(statuses = listOf("SUBMITTED", "ACCEPTED"))

      assertThatJson(result).matchesExpectedJson(
        expectedSearchExternalReferralResponseBody(
          listOf(
            expectedResponseBodyFor(newest),
            expectedResponseBodyFor(oldest),
          ),
        ),
      )
    }

    @Test
    fun `should filter referrals by a single status`() {
      val submitted = externalReferralRepository.save(
        buildExternalReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityExternalReferralStatus.SUBMITTED,
        ),
      )

      externalReferralRepository.save(
        buildExternalReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityExternalReferralStatus.REJECTED,
        ),
      )

      val result = searchExternalReferrals(statuses = listOf("SUBMITTED"))

      assertThatJson(result).matchesExpectedJson(
        expectedSearchExternalReferralResponseBody(
          listOf(expectedResponseBodyFor(submitted)),
        ),
      )
    }

    @Test
    fun `should filter referrals by multiple statuses`() {
      val submitted = externalReferralRepository.save(
        buildExternalReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityExternalReferralStatus.SUBMITTED,
          submissionDate = LocalDate.of(2026, 1, 10),
        ),
      )
      val accepted = externalReferralRepository.save(
        buildExternalReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityExternalReferralStatus.ACCEPTED,
          submissionDate = LocalDate.of(2026, 1, 20),
        ),
      )
      externalReferralRepository.save(
        buildExternalReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityExternalReferralStatus.REJECTED,
        ),
      )

      val result = searchExternalReferrals(statuses = listOf("SUBMITTED", "ACCEPTED"))

      assertThatJson(result).matchesExpectedJson(
        expectedSearchExternalReferralResponseBody(
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
      val referralByTestDataSetupUser = externalReferralRepository.save(
        buildExternalReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityExternalReferralStatus.SUBMITTED,
          submissionDate = LocalDate.of(2026, 1, 1),
        ),
      )
      val referralByDeliusUser = AuditOverrideContext.withAuditorId(userIdOfLoggedInDeliusUser) {
        externalReferralRepository.save(
          buildExternalReferralEntity(
            caseId = case.id,
            crn = crn,
            status = EntityExternalReferralStatus.ACCEPTED,
            submissionDate = LocalDate.of(2026, 2, 1),
          ),
        )
      }

      val result = searchExternalReferrals(statuses = listOf("SUBMITTED", "ACCEPTED"))

      assertThatJson(result).matchesExpectedJson(
        expectedSearchExternalReferralResponseBody(
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
      val result = searchExternalReferrals()

      assertThatJson(result).matchesExpectedJson(
        expectedSearchExternalReferralResponseBody(emptyList()),
      )
    }

    @Test
    fun `should return populated withdrawalReason and outcomeNote for an accepted referral`() {
      val accepted = externalReferralRepository.save(
        buildExternalReferralEntity(
          caseId = case.id,
          crn = crn,
          status = EntityExternalReferralStatus.ACCEPTED,
          withdrawalReason = EntityExternalReferralWithdrawalReason.ACCEPTED_BY_ORGANISATION,
          outcomeNote = "An outcome note",
        ),
      )

      val result = searchExternalReferrals(statuses = listOf("ACCEPTED"))

      assertThatJson(result).matchesExpectedJson(
        expectedSearchExternalReferralResponseBody(
          listOf(expectedResponseBodyFor(accepted)),
        ),
      )
    }

    @Test
    fun `should return 403 on search when user does not have required role`() {
      restTestClient.get().uri("/cases/{crn}/external-referral/search", crn)
        .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
        .exchange()
        .expectStatus().isForbidden
    }

    private fun searchExternalReferrals(statuses: List<String>? = null): String = restTestClient.get().uri {
      it.path("/cases/$crn/external-referral/search")
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
      entity: ExternalReferralEntity,
      createdBy: String = NAME_OF_TEST_DATA_SETUP_USER,
      createdByUsername: String = USERNAME_OF_TEST_DATA_SETUP_USER,
    ): String = expectedExternalReferralResponseBody(
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
      withdrawalReason = entity.withdrawalReason?.name,
      outcomeNote = entity.outcomeNote,
    )
  }

  @Nested
  inner class UpdateExternalReferral {
    @Test
    fun `should update external referral and return 200 with updated data`() {
      val existingEntity = createExternalReferralEntity(
        referenceNumber = "REF-001",
        organisationName = "Organisation name",
        website = "https://www.charity.org",
        submissionNote = "A submission note",
        email = "contact@example.com",
        phoneNumber = "01234567890",
      )

      val result = restTestClient.put().uri("/cases/$crn/external-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
            submissionDate = "2026-01-20",
            referenceNumber = "REF-002",
            organisationName = "New organisation name",
            website = "https://www.new-charity.org",
            submissionNote = "An updated submission note",
            email = "new-contact@example.com",
            phoneNumber = "09876543210",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      val updatedRecord = externalReferralRepository.findByCaseId(case.id)!!
      assertThat(updatedRecord.referenceNumber).isEqualTo("REF-002")
      assertThat(updatedRecord.submissionDate).isEqualTo(LocalDate.of(2026, 1, 20))
      assertThat(updatedRecord.organisationName).isEqualTo("New organisation name")
      assertThat(updatedRecord.website).isEqualTo("https://www.new-charity.org")
      assertThat(updatedRecord.submissionNote).isEqualTo("An updated submission note")
      assertThat(updatedRecord.email).isEqualTo("new-contact@example.com")
      assertThat(updatedRecord.phoneNumber).isEqualTo("09876543210")

      assertThatJson(result).matchesExpectedJson(
        expectedExternalReferralResponseBody(
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
          email = "new-contact@example.com",
          phoneNumber = "09876543210",
        ),
      )
    }

    @Test
    fun `should return 404 when updating nonexistent external referral`() {
      val nonExistentId = UUID.randomUUID()

      restTestClient.put().uri("/cases/$crn/external-referral/$nonExistentId")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createExternalReferralRequestBody())
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 404 when updating external referral with CRN that does not match`() {
      val existingEntity = createExternalReferralEntity()

      restTestClient.put().uri("/cases/OTHERCRN/external-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createExternalReferralRequestBody())
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 403 when user does not have the required role for update`() {
      val existingEntity = createExternalReferralEntity()

      restTestClient.put().uri("/cases/$crn/external-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createExternalReferralRequestBody())
        .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should accept external referral with an outcome reason and note`() {
      val existingEntity = createExternalReferralEntity()

      val result = restTestClient.put().uri("/cases/$crn/external-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
            status = ExternalReferralStatus.ACCEPTED.name,
            withdrawalReason = ExternalReferralWithdrawalReason.ACCEPTED_WITH_ACCOMMODATION_PLACEMENT.name,
            outcomeNote = "An outcome note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      assertThatJson(result).matchesExpectedJson(
        expectedExternalReferralResponseBody(
          id = existingEntity.id,
          caseId = case.id,
          crn = crn,
          status = ExternalReferralStatus.ACCEPTED.name,
          createdBy = NAME_OF_TEST_DATA_SETUP_USER,
          createdByUsername = USERNAME_OF_TEST_DATA_SETUP_USER,
          createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          withdrawalReason = ExternalReferralWithdrawalReason.ACCEPTED_WITH_ACCOMMODATION_PLACEMENT.name,
          outcomeNote = "An outcome note",
        ),
      )
    }

    @Test
    fun `should reject external referral with an outcome reason and note`() {
      val existingEntity = createExternalReferralEntity()

      val result = restTestClient.put().uri("/cases/$crn/external-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
            status = ExternalReferralStatus.REJECTED.name,
            withdrawalReason = ExternalReferralWithdrawalReason.NO_CAPACITY.name,
            outcomeNote = "An outcome note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      assertThatJson(result).matchesExpectedJson(
        expectedExternalReferralResponseBody(
          id = existingEntity.id,
          caseId = case.id,
          crn = crn,
          status = ExternalReferralStatus.REJECTED.name,
          createdBy = NAME_OF_TEST_DATA_SETUP_USER,
          createdByUsername = USERNAME_OF_TEST_DATA_SETUP_USER,
          createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          withdrawalReason = ExternalReferralWithdrawalReason.NO_CAPACITY.name,
          outcomeNote = "An outcome note",
        ),
      )
    }

    @Test
    fun `should return 400 when accepting external referral without an outcome reason`() {
      val existingEntity = createExternalReferralEntity()

      restTestClient.put().uri("/cases/$crn/external-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
            status = ExternalReferralStatus.ACCEPTED.name,
          ),
        )
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `should return 400 when accepting external referral with a REJECTED-only outcome reason`() {
      val existingEntity = createExternalReferralEntity()

      restTestClient.put().uri("/cases/$crn/external-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
            status = ExternalReferralStatus.ACCEPTED.name,
            withdrawalReason = ExternalReferralWithdrawalReason.NO_CAPACITY.name,
          ),
        )
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `should return 400 when providing an outcome reason for a SUBMITTED status`() {
      val existingEntity = createExternalReferralEntity()

      restTestClient.put().uri("/cases/$crn/external-referral/${existingEntity.id}")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
            status = ExternalReferralStatus.SUBMITTED.name,
            withdrawalReason = ExternalReferralWithdrawalReason.ACCEPTED_BY_ORGANISATION.name,
          ),
        )
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isBadRequest
    }
  }

  @Nested
  inner class CreateExternalReferralNote {
    @Test
    fun `should create a note for external referral`() {
      val existingEntity = createExternalReferralEntity()
      val note1Value = "Test note 1"
      val note2Value = "Test note 2"

      restTestClient.post().uri("/cases/$crn/external-referral/${existingEntity.id}/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .body(externalReferralNoteRequestBody(note = note1Value))
        .withDeliusUserJwt()
        .exchangeSuccessfully()

      var persistedResult = externalReferralRepository.findByIdAndCrnWithNotes(existingEntity.id, crn)!!
      assertThat(persistedResult.notes.first().note).isEqualTo(note1Value)

      restTestClient.post().uri("/cases/$crn/external-referral/${existingEntity.id}/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .body(externalReferralNoteRequestBody(note = note2Value))
        .withDeliusUserJwt()
        .exchangeSuccessfully()

      persistedResult = externalReferralRepository.findByIdAndCrnWithNotes(existingEntity.id, crn)!!
      val sortedNotes = persistedResult.notes.sortedByDescending { it.createdAt }
      assertThat(sortedNotes.first().note).isEqualTo(note2Value)
      assertThat(sortedNotes[1].note).isEqualTo(note1Value)
    }

    @Test
    fun `should not create a note when external referral not found`() {
      restTestClient.post().uri("/cases/$crn/external-referral/${UUID.randomUUID()}/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .body(externalReferralNoteRequestBody(note = "Test note"))
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should not create a note when crn not found`() {
      val existingEntity = createExternalReferralEntity()

      restTestClient.post().uri("/cases/${UUID.randomUUID()}/external-referral/${existingEntity.id}/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .body(externalReferralNoteRequestBody(note = "Test note"))
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should fail with Bad Request for empty note`() {
      val existingEntity = createExternalReferralEntity()

      restTestClient.post().uri("/cases/$crn/external-referral/${existingEntity.id}/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .body(externalReferralNoteRequestBody(note = ""))
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `should fail with Bad Request for note exceeding 4000 characters`() {
      val existingEntity = createExternalReferralEntity()

      restTestClient.post().uri("/cases/$crn/external-referral/${existingEntity.id}/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .body(externalReferralNoteRequestBody(note = "a".repeat(4001)))
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `should return 403 for createNote when user does not have the required role`() {
      val existingEntity = createExternalReferralEntity()

      restTestClient.post().uri("/cases/$crn/external-referral/${existingEntity.id}/notes")
        .contentType(MediaType.APPLICATION_JSON)
        .body(externalReferralNoteRequestBody(note = "Test note"))
        .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Nested
  inner class GetExternalReferralTimeline {
    @Test
    fun `should return external referral timeline when an external referral is created`() {
      val createdExternalReferral = restTestClient.post().uri("/cases/{crn}/external-referral", crn)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
            submissionDate = "2026-01-15",
            referenceNumber = "REF-001",
            submissionNote = "A submission note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      val createdExternalReferralId = ObjectMapper().readTree(createdExternalReferral).get("submission").get("id").asText()
      val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdExternalReferralId))
      assertThat(commitTimesAsc).hasSize(1)

      restTestClient.get().uri("/cases/{crn}/external-referral/{id}/timeline", crn, createdExternalReferralId)
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .value {
          assertThatJson(it!!).matchesExpectedJson(
            expectedGetExternalReferralTimelineResponse(
              externalReferralId = UUID.fromString(createdExternalReferralId),
              caseId = case.id,
              crn = crn,
              createCommitTime = commitTimesAsc.first().truncatedTo(ChronoUnit.SECONDS).toString(),
            ),
          )
        }
    }

    @Test
    fun `should return external referral timeline with notes and updates`() {
      val createdExternalReferral = restTestClient.post().uri("/cases/{crn}/external-referral", crn)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
            submissionDate = "2026-01-15",
            referenceNumber = "REF-001",
            submissionNote = "A submission note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      val createdExternalReferralId = ObjectMapper().readTree(createdExternalReferral).get("submission").get("id").asText()

      restTestClient.post().uri("/cases/{crn}/external-referral/{id}/notes", crn, createdExternalReferralId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(externalReferralNoteRequestBody(note = "Test note"))
        .withDeliusUserJwt()
        .exchangeSuccessfully()

      restTestClient.put().uri("/cases/{crn}/external-referral/{id}", crn, createdExternalReferralId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
            submissionDate = "2026-01-15",
            referenceNumber = "REF-002",
            submissionNote = "A submission note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()

      val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdExternalReferralId))
      assertThat(commitTimesAsc).hasSize(2)
      val createNoteCommitTime = externalReferralRepository.findByIdAndCrnWithNotes(UUID.fromString(createdExternalReferralId), crn)!!
        .notes.first().createdAt

      restTestClient.get().uri("/cases/{crn}/external-referral/{id}/timeline", crn, createdExternalReferralId)
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .value {
          assertThatJson(it!!).matchesExpectedJson(
            expectedGetExternalReferralTimelineResponse(
              externalReferralId = UUID.fromString(createdExternalReferralId),
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
    fun `should return external referral timeline when it is accepted with an outcome reason and note`() {
      val createdExternalReferral = restTestClient.post().uri("/cases/{crn}/external-referral", crn)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
            submissionDate = "2026-01-15",
            referenceNumber = "REF-001",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      val createdExternalReferralId = ObjectMapper().readTree(createdExternalReferral).get("submission").get("id").asText()

      restTestClient.put().uri("/cases/{crn}/external-referral/{id}", crn, createdExternalReferralId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
            submissionDate = "2026-01-15",
            referenceNumber = "REF-001",
            status = EntityExternalReferralStatus.ACCEPTED.name,
            withdrawalReason = EntityExternalReferralWithdrawalReason.ACCEPTED_WITH_ACCOMMODATION_PLACEMENT.name,
            outcomeNote = "An outcome note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()

      val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdExternalReferralId))
      assertThat(commitTimesAsc).hasSize(2)

      restTestClient.get().uri("/cases/{crn}/external-referral/{id}/timeline", crn, createdExternalReferralId)
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .value {
          assertThatJson(it!!).matchesExpectedJson(
            expectedGetExternalReferralOutcomeTimelineResponse(
              externalReferralId = UUID.fromString(createdExternalReferralId),
              caseId = case.id,
              crn = crn,
              createCommitTime = commitTimesAsc.first().truncatedTo(ChronoUnit.SECONDS).toString(),
              updateCommitTime = commitTimesAsc[1].truncatedTo(ChronoUnit.SECONDS).toString(),
              newStatus = EntityExternalReferralStatus.ACCEPTED.name,
              withdrawalReason = EntityExternalReferralWithdrawalReason.ACCEPTED_WITH_ACCOMMODATION_PLACEMENT.name,
              outcomeNote = "An outcome note",
            ),
          )
        }
    }

    @Test
    fun `should return external referral timeline when it is rejected with an outcome reason and note`() {
      val createdExternalReferral = restTestClient.post().uri("/cases/{crn}/external-referral", crn)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
            submissionDate = "2026-01-15",
            referenceNumber = "REF-001",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .returnResult().responseBody!!

      val createdExternalReferralId = ObjectMapper().readTree(createdExternalReferral).get("submission").get("id").asText()

      restTestClient.put().uri("/cases/{crn}/external-referral/{id}", crn, createdExternalReferralId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          createExternalReferralRequestBody(
            submissionDate = "2026-01-15",
            referenceNumber = "REF-001",
            status = EntityExternalReferralStatus.REJECTED.name,
            withdrawalReason = EntityExternalReferralWithdrawalReason.NO_CAPACITY.name,
            outcomeNote = "Another outcome note",
          ),
        )
        .withDeliusUserJwt()
        .exchangeSuccessfully()

      val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdExternalReferralId))
      assertThat(commitTimesAsc).hasSize(2)

      restTestClient.get().uri("/cases/{crn}/external-referral/{id}/timeline", crn, createdExternalReferralId)
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody<String>()
        .value {
          assertThatJson(it!!).matchesExpectedJson(
            expectedGetExternalReferralOutcomeTimelineResponse(
              externalReferralId = UUID.fromString(createdExternalReferralId),
              caseId = case.id,
              crn = crn,
              createCommitTime = commitTimesAsc.first().truncatedTo(ChronoUnit.SECONDS).toString(),
              updateCommitTime = commitTimesAsc[1].truncatedTo(ChronoUnit.SECONDS).toString(),
              newStatus = EntityExternalReferralStatus.REJECTED.name,
              withdrawalReason = EntityExternalReferralWithdrawalReason.NO_CAPACITY.name,
              outcomeNote = "Another outcome note",
            ),
          )
        }
    }

    @Test
    fun `should return 404 for timeline when external referral not found`() {
      restTestClient.get().uri("/cases/{crn}/external-referral/{id}/timeline", crn, UUID.randomUUID())
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 404 for timeline when crn does not match`() {
      val entity = createExternalReferralEntity()

      restTestClient.get().uri("/cases/{crn}/external-referral/{id}/timeline", "OTHERCRN", entity.id)
        .withDeliusUserJwt()
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 403 for timeline when user does not have the required role`() {
      val entity = createExternalReferralEntity()

      restTestClient.get().uri("/cases/{crn}/external-referral/{id}/timeline", crn, entity.id)
        .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
        .exchange()
        .expectStatus().isForbidden
    }

    private fun getCommitTimesAsc(externalReferralId: UUID): List<Instant> {
      val changes = javers.findChanges(
        QueryBuilder.byInstanceId(externalReferralId, ExternalReferralEntity::class.java).build(),
      )
      return changes.groupBy { it.commitMetadata.get().id }.entries
        .map { (_, commitChanges) -> commitChanges.first().commitMetadata.get().commitDateInstant }
        .sorted()
    }
  }

  private fun createExternalReferralEntity(
    referenceNumber: String? = "OA-REF-001",
    submissionDate: LocalDate = LocalDate.of(2026, 1, 15),
    status: EntityExternalReferralStatus = EntityExternalReferralStatus.SUBMITTED,
    organisationName: String? = null,
    website: String? = null,
    submissionNote: String? = null,
    email: String? = null,
    phoneNumber: String? = null,
  ): ExternalReferralEntity = externalReferralRepository.save(
    buildExternalReferralEntity(
      crn = crn,
      caseId = case.id,
      referenceNumber = referenceNumber,
      submissionDate = submissionDate,
      status = status,
      organisationName = organisationName,
      website = website,
      submissionNote = submissionNote,
      email = email,
      phoneNumber = phoneNumber,
    ),
  )
}
