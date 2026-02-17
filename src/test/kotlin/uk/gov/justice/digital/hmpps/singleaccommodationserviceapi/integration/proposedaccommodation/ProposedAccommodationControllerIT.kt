package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedGetProposedAccommodationByIdResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedGetProposedAccommodationsResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedProposedAddressesResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedSasAddressUpdatedDomainEventJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.proposedAddressesRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.TestSqsDomainEventListener
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus as EntityNextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus as EntityVerificationStatus

class ProposedAccommodationControllerIT : IntegrationTestBase() {
  @Autowired
  lateinit var testSqsDomainEventListener: TestSqsDomainEventListener

  @Autowired
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @Autowired
  lateinit var outboxEventRepository: OutboxEventRepository

  private val crn = "FAKECRN1"

  @BeforeEach
  fun setup() {
    proposedAccommodationRepository.deleteAll()
    outboxEventRepository.deleteAll()
    hmppsAuth.stubGrantToken()
  }

  @AfterEach
  fun teardown() {
    proposedAccommodationRepository.deleteAll()
    outboxEventRepository.deleteAll()
  }

  @Test
  fun `should get proposed-accommodation for crn`() {
    val olderTimestamp = Instant.parse("2025-01-01T10:00:00Z")
    val newerTimestamp = Instant.parse("2025-01-02T10:00:00Z")

    val olderEntity = createAndSaveProposedAccommodation(
      postcode = "RG26 5AG",
      buildingNumber = "4",
      thoroughfareName = "Dollis Green",
      postTown = "Bramley",
      createdAt = olderTimestamp,
    )

    val newerEntity = createAndSaveProposedAccommodation(
      postcode = "W1 8XX",
      buildingNumber = "11",
      thoroughfareName = "Piccadilly Circus",
      postTown = "London",
      createdAt = newerTimestamp,
    )

    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetProposedAccommodationsResponse(
            firstId = newerEntity.id,
            firstCreatedAt = newerEntity.createdAt.toString(),
            secondId = olderEntity.id,
            secondCreatedAt = olderEntity.createdAt.toString(),
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
      createdAt = Instant.parse("2025-01-01T10:00:00Z"),
    )

    restTestClient.get().uri("/cases/{crn}/proposed-accommodations/{id}", crn, entity.id)
      .withJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetProposedAccommodationByIdResponse(
            id = entity.id,
            createdAt = entity.createdAt.toString(),
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
      createdAt = Instant.parse("2025-01-01T10:00:00Z"),
    )

    restTestClient.get().uri("/proposed-accommodations/{id}", entity.id)
      .withJwt(roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE__ACCOMMODATION_DATA_DOMAIN"))
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetProposedAccommodationByIdResponse(
            id = entity.id,
            createdAt = entity.createdAt.toString(),
          ),
        )
      }
  }

  @Test
  fun `should return 404 when proposed-accommodation not found for ADDA role`() {
    val nonExistentId = UUID.randomUUID()

    restTestClient.get().uri("/proposed-accommodations/{id}", nonExistentId)
      .withJwt(roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE__ACCOMMODATION_DATA_DOMAIN"))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 403 when using ROLE_PROBATION for ADDA endpoint`() {
    val nonExistentId = UUID.randomUUID()

    restTestClient.get().uri("/proposed-accommodations/{id}", nonExistentId)
      .withJwt(roles = listOf("ROLE_PROBATION"))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return 404 when proposed-accommodation not found`() {
    val nonExistentId = UUID.randomUUID()

    restTestClient.get().uri("/cases/{crn}/proposed-accommodations/{id}", crn, nonExistentId)
      .withJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return empty list when no proposed-accommodations exist for crn`() {
    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withJwt()
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
          verificationStatus = EntityVerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
        ),
      )
      .withJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .returnResult().responseBody!!

    val proposedAccommodationPersistedResult = proposedAccommodationRepository.getByCrn(crn)!!
    assertPersistedProposedAccommodation(proposedAccommodationPersistedResult)

    assertThatJson(result).matchesExpectedJson(
      expectedJson = expectedProposedAddressesResponseBody(
        id = proposedAccommodationPersistedResult.id,
        verificationStatus = VerificationStatus.PASSED.name,
        nextAccommodationStatus = NextAccommodationStatus.YES.name,
        createdAt = proposedAccommodationPersistedResult.createdAt.truncatedTo(ChronoUnit.SECONDS).toString(),
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
        crn = crn,
        name = "Old Name",
        arrangementSubType = AccommodationArrangementSubType.OTHER,
        arrangementSubTypeDescription = "Old description",
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
        offenderReleaseType = OffenderReleaseType.REMAND,
        createdAt = Instant.parse("2025-06-01T10:00:00Z"),
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
      .withJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .returnResult().responseBody!!

    assertThatJson(result).matchesExpectedJson(
      expectedJson = expectedProposedAddressesResponseBody(
        id = existingEntity.id,
        verificationStatus = VerificationStatus.PASSED.name,
        nextAccommodationStatus = NextAccommodationStatus.YES.name,
        createdAt = existingEntity.createdAt.truncatedTo(ChronoUnit.SECONDS).toString(),
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
        crn = crn,
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
        createdAt = Instant.parse("2025-06-01T10:00:00Z"),
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
      .withJwt()
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
      .withJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 404 when CRN does not match proposed-accommodation`() {
    val existingEntity = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        crn = "DIFFERENT_CRN",
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
      ),
    )

    restTestClient.put().uri("/cases/$crn/proposed-accommodations/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          verificationStatus = EntityVerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
        ),
      )
      .withJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should preserve original createdAt when updating proposed-accommodation`() {
    val originalCreatedAt = Instant.parse("2025-06-01T10:00:00Z")
    val existingEntity = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        crn = crn,
        arrangementSubType = AccommodationArrangementSubType.OTHER,
        arrangementSubTypeDescription = "Old description",
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
        createdAt = originalCreatedAt,
      ),
    )

    val result = restTestClient.put().uri("/cases/$crn/proposed-accommodations/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET.name,
          nextAccommodationStatus = NextAccommodationStatus.NO.name,
        ),
      )
      .withJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .returnResult().responseBody!!

    assertThatJson(result).matchesExpectedJson(
      expectedJson = expectedProposedAddressesResponseBody(
        id = existingEntity.id,
        verificationStatus = VerificationStatus.NOT_CHECKED_YET.name,
        nextAccommodationStatus = NextAccommodationStatus.NO.name,
        createdAt = originalCreatedAt.truncatedTo(ChronoUnit.SECONDS).toString(),
      ),
    )
  }

  private fun createAndSaveProposedAccommodation(
    postcode: String,
    buildingNumber: String,
    thoroughfareName: String,
    postTown: String,
    createdAt: Instant,
  ): ProposedAccommodationEntity {
    val entity = buildProposedAccommodationEntity(
      crn = crn,
      postcode = postcode,
      buildingNumber = buildingNumber,
      throughfareName = thoroughfareName,
      postTown = postTown,
      createdAt = createdAt,
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
