package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildNomisUserDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_TEST_DATA_SETUP_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_NOMIS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedGetProposedAccommodationByIdResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedGetProposedAccommodationTimelineResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedGetProposedAccommodationsResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedProposedAddressesResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedSasAddressUpdatedDomainEventJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.proposedAddressesRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.NomisUserRolesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.TestSqsDomainEventListener
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus as EntityNextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus as EntityVerificationStatus

class ProposedAccommodationControllerIT : IntegrationTestBase() {
  @Autowired
  private lateinit var testSqsDomainEventListener: TestSqsDomainEventListener

  @Autowired
  private lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @Autowired
  private lateinit var outboxEventRepository: OutboxEventRepository

  @Autowired
  private lateinit var caseRepository: CaseRepository

  @Autowired
  private lateinit var javers: Javers

  private lateinit var crn: String

  private lateinit var beforeTest: Instant
  private lateinit var caseEntity: CaseEntity

  @BeforeEach
  fun setup() {
    beforeTest = Instant.now()
    proposedAccommodationRepository.deleteAll()
    outboxEventRepository.deleteAll()
    crn = UUID.randomUUID().toString()
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })

    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()
  }

  @AfterEach
  fun teardown() {
    proposedAccommodationRepository.deleteAll()
    outboxEventRepository.deleteAll()
  }

  @Test
  fun `should get proposed-accommodation for crn`() {
    val olderEntity = createAndSaveProposedAccommodation(
      postcode = "RG26 5AG",
      buildingNumber = "4",
      thoroughfareName = "Dollis Green",
      postTown = "Bramley",
    )

    val newerEntity = createAndSaveProposedAccommodation(
      postcode = "W1 8XX",
      buildingNumber = "11",
      thoroughfareName = "Piccadilly Circus",
      postTown = "London",
    )

    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetProposedAccommodationsResponse(
            firstId = newerEntity.id,
            firstCreatedAt = newerEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
            secondId = olderEntity.id,
            secondCreatedAt = olderEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
            crn = crn,
            caseId = caseEntity.id,
          ),
        )
      }
  }

  @Test
  fun `should get proposed-accommodation by id and crn`() {
    val entity = createAndSaveProposedAccommodation(
      postcode = "W1 8XX",
      buildingNumber = "11",
      thoroughfareName = "Piccadilly Circus",
      postTown = "London",
    )

    restTestClient.get().uri("/cases/{crn}/proposed-accommodations/{id}", crn, entity.id)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetProposedAccommodationByIdResponse(
            id = entity.id,
            caseId = entity.caseId,
            crn = crn,
            createdAt = entity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          ),
        )
      }
  }

  @Test
  fun `should get proposed-accommodation by id with ADDA role`() {
    val entity = createAndSaveProposedAccommodation(
      postcode = "W1 8XX",
      buildingNumber = "11",
      thoroughfareName = "Piccadilly Circus",
      postTown = "London",
    )

    restTestClient.get().uri("/proposed-accommodations/{id}", entity.id)
      .withClientCredentialsJwt(
        roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE__ACCOMMODATION_DATA_DOMAIN"),
      )
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetProposedAccommodationByIdResponse(
            id = entity.id,
            caseId = entity.caseId,
            crn = crn,
            createdAt = entity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
          ),
        )
      }
  }

  @Test
  fun `should return 404 when proposed-accommodation not found for ADDA role`() {
    val nonExistentId = UUID.randomUUID()

    restTestClient.get().uri("/proposed-accommodations/{id}", nonExistentId)
      .withDeliusUserJwt(roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE__ACCOMMODATION_DATA_DOMAIN"))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 403 when using token with SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER for ADDA endpoint`() {
    val nonExistentId = UUID.randomUUID()

    restTestClient.get().uri("/proposed-accommodations/{id}", nonExistentId)
      .withDeliusUserJwt(roles = listOf("SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER"))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return 404 when proposed-accommodation not found`() {
    val nonExistentId = UUID.randomUUID()

    restTestClient.get().uri("/cases/{crn}/proposed-accommodations/{id}", crn, nonExistentId)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return empty list when no proposed-accommodations exist for crn`() {
    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson("[]")
      }
  }

  @Test
  fun `should create proposed-accommodation and publish sas-address-updated event`() {
    val result = restTestClient.post().uri("/cases/$crn/proposed-accommodations")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .returnResult().responseBody!!

    val proposedAccommodationPersistedResult =
      proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn).first()
    assertPersistedProposedAccommodation(proposedAccommodationPersistedResult)

    assertThatJson(result).matchesExpectedJson(
      expectedJson = expectedProposedAddressesResponseBody(
        caseId = caseEntity.id,
        crn = crn,
        id = proposedAccommodationPersistedResult.id,
        verificationStatus = VerificationStatus.PASSED.name,
        nextAccommodationStatus = NextAccommodationStatus.YES.name,
        createdBy = NAME_OF_LOGGED_IN_DELIUS_USER,
        createdAt = proposedAccommodationPersistedResult.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
      ),
    )
    assertPublishedSNSEvent(
      proposedAccommodationId = proposedAccommodationPersistedResult.id,
      eventType = SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_UPDATED,
      eventDescription = SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_UPDATED.typeDescription,
    )
    assertThatOutboxIsAsExpected(
      proposedAccommodationId = proposedAccommodationPersistedResult.id,
    )
  }

  @Test
  fun `should update proposed-accommodation and return 200 with updated data`() {
    val existingEntity = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        name = "Old Name",
        caseId = caseEntity.id,
        arrangementSubType = AccommodationArrangementSubType.OTHER,
        arrangementSubTypeDescription = "Old description",
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
        offenderReleaseType = OffenderReleaseType.REMAND,
      ),
    )

    val result = restTestClient.put().uri("/cases/$crn/proposed-accommodations/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          verificationStatus = EntityVerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .returnResult().responseBody!!

    assertThatJson(result).matchesExpectedJson(
      expectedJson = expectedProposedAddressesResponseBody(
        id = existingEntity.id,
        verificationStatus = VerificationStatus.PASSED.name,
        nextAccommodationStatus = NextAccommodationStatus.YES.name,
        createdBy = NAME_OF_TEST_DATA_SETUP_USER,
        createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        caseId = caseEntity.id,
        crn = crn,
      ),
    )

    assertPublishedSNSEvent(
      proposedAccommodationId = existingEntity.id,
      eventType = SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_UPDATED,
      eventDescription = SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_UPDATED.typeDescription,
    )
    assertThatOutboxIsAsExpected(
      proposedAccommodationId = existingEntity.id,
    )
  }

  @Test
  fun `should update proposed-accommodation and not publish domain event when nextAccommodationStatus is NO`() {
    val existingEntity = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        caseId = caseEntity.id,
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
      ),
    )

    restTestClient.put().uri("/cases/$crn/proposed-accommodations/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET.name,
          nextAccommodationStatus = NextAccommodationStatus.NO.name,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    assertThat(outboxEventRepository.findAll()).isEmpty()
  }

  @Test
  fun `should return 404 when updating nonexistent proposed-accommodation`() {
    val nonExistentId = UUID.randomUUID()

    restTestClient.put().uri("/cases/$crn/proposed-accommodations/$nonExistentId")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          verificationStatus = EntityVerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 404 when CRN does not match proposed-accommodation`() {
    val existingEntity = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        caseId = caseEntity.id,
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
      ),
    )
    val otherCase = caseRepository.save(buildCaseEntity { withCrn(UUID.randomUUID().toString()) })
    val otherEntity = proposedAccommodationRepository.save(buildProposedAccommodationEntity(caseId = otherCase.id))

    restTestClient.put().uri("/cases/$crn/proposed-accommodations/${otherEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          verificationStatus = EntityVerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return proposed accommodation timeline when it is created and then updated a couple of times`() {
    val createdProposedAccommodation = restTestClient.post()
      .uri("/cases/{crn}/proposed-accommodations", crn)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.NO.name,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .returnResult().responseBody!!

    val createdProposedAccommodationId = ObjectMapper()
      .readTree(createdProposedAccommodation)
      .get("id").asText()

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

    restTestClient.put().uri("/cases/{crn}/proposed-accommodations/{id}", crn, createdProposedAccommodationId)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          verificationStatus = VerificationStatus.FAILED.name,
          nextAccommodationStatus = NextAccommodationStatus.NO.name,
        ),
      )
      .withNomisUserJwt()
      .exchangeSuccessfully()

    restTestClient.put().uri("/cases/{crn}/proposed-accommodations/{id}", crn, createdProposedAccommodationId)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          postcode = "correct postcode",
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdProposedAccommodationId))
    assertThat(commitTimesAsc).hasSize(3)

    restTestClient.get().uri("/cases/{crn}/proposed-accommodations/{id}/timeline", crn, createdProposedAccommodationId)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetProposedAccommodationTimelineResponse(
            proposedAccommodationId = UUID.fromString(createdProposedAccommodationId),
            caseId = caseEntity.id,
            createCommitTime = commitTimesAsc.first().toString(),
            update1CommitTime = commitTimesAsc[1].toString(),
            update2CommitTime = commitTimesAsc[2].toString(),
          ),
        )
      }
  }

  private fun getCommitTimesAsc(createdProposedAccommodationId: UUID): List<Instant> {
    val changes = javers.findChanges(
      QueryBuilder.byInstanceId(createdProposedAccommodationId, ProposedAccommodationEntity::class.java).build(),
    )
    return changes.groupBy {
      it.commitMetadata.get().id
    }.entries
      .map { (_, commitChanges) ->
        commitChanges.first().commitMetadata.get().commitDateInstant
      }.sorted()
  }

  private fun createAndSaveProposedAccommodation(
    postcode: String,
    buildingNumber: String,
    thoroughfareName: String,
    postTown: String,
  ): ProposedAccommodationEntity {
    val entity = buildProposedAccommodationEntity(
      caseId = caseEntity.id,
      postcode = postcode,
      buildingNumber = buildingNumber,
      throughfareName = thoroughfareName,
      postTown = postTown,
    )
    return proposedAccommodationRepository.save(entity)
  }

  private fun assertPersistedProposedAccommodation(proposedAccommodationEntity: ProposedAccommodationEntity) {
    assertThat(proposedAccommodationEntity.name).isEqualTo("Mother's caravan")
    assertThat(proposedAccommodationEntity.arrangementType).isEqualTo(AccommodationArrangementType.PRIVATE)
    assertThat(proposedAccommodationEntity.arrangementSubType).isEqualTo(AccommodationArrangementSubType.OTHER)
    assertThat(proposedAccommodationEntity.arrangementSubTypeDescription).isEqualTo("Caravan site")
    assertThat(proposedAccommodationEntity.settledType).isEqualTo(AccommodationSettledType.SETTLED)
    assertThat(proposedAccommodationEntity.offenderReleaseType).isEqualTo(OffenderReleaseType.REMAND)
    assertThat(proposedAccommodationEntity.verificationStatus).isEqualTo(EntityVerificationStatus.PASSED)
    assertThat(proposedAccommodationEntity.nextAccommodationStatus).isEqualTo(EntityNextAccommodationStatus.YES)
    assertThat(proposedAccommodationEntity.postcode).isEqualTo("test postcode")
    assertThat(proposedAccommodationEntity.subBuildingName).isEqualTo("test sub building name")
    assertThat(proposedAccommodationEntity.buildingName).isEqualTo("test building name")
    assertThat(proposedAccommodationEntity.buildingNumber).isEqualTo("4")
    assertThat(proposedAccommodationEntity.throughfareName).isEqualTo("test thoroughfareName")
    assertThat(proposedAccommodationEntity.dependentLocality).isEqualTo("test dependent locality")
    assertThat(proposedAccommodationEntity.postTown).isEqualTo("test post town")
    assertThat(proposedAccommodationEntity.county).isEqualTo("test county")
    assertThat(proposedAccommodationEntity.uprn).isEqualTo("UP123454")
    assertThat(proposedAccommodationEntity.startDate).isEqualTo(LocalDate.of(2026, 1, 5))
    assertThat(proposedAccommodationEntity.endDate).isEqualTo(LocalDate.of(2026, 4, 25))
    assertThat(proposedAccommodationEntity.createdByUserId).isEqualTo(userIdOfLoggedInDeliusUser)
    assertThat(proposedAccommodationEntity.createdAt).isBetween(
      beforeTest.minusSeconds(1),
      Instant.now().plusSeconds(1),
    )
  }

  private fun assertPublishedSNSEvent(
    proposedAccommodationId: UUID,
    eventType: SingleAccommodationServiceDomainEventType,
    eventDescription: String,
    detailUrl: String = "http://api-host/proposed-accommodations",
  ) {
    val emittedMessage = testSqsDomainEventListener.blockForMessage(eventType)
    assertThat(emittedMessage.description).isEqualTo(eventDescription)
    assertThat(emittedMessage.detailUrl).isEqualTo("$detailUrl/$proposedAccommodationId")
  }

  private fun assertThatOutboxIsAsExpected(proposedAccommodationId: UUID) {
    val outboxRecord = outboxEventRepository.findAll().first()
    assertThat(outboxRecord.aggregateId).isEqualTo(proposedAccommodationId)
    assertThat(outboxRecord.aggregateType).isEqualTo("ProposedAccommodation")
    assertThat(outboxRecord.domainEventType).isEqualTo(SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_UPDATED.name)
    assertThatJson(outboxRecord.payload).matchesExpectedJson(expectedSasAddressUpdatedDomainEventJson(proposedAccommodationId))
    assertThat(outboxRecord.processedStatus).isEqualTo(ProcessedStatus.SUCCESS)
  }
}
