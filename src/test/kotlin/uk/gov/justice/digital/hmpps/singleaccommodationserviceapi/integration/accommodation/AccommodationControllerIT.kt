package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodation

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodation.json.expectedGetAccommodationByIdResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodation.json.expectedGetCurrentAccommodationResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodation.json.expectedGetCurrentAccommodationWithUpstreamFailureResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class AccommodationControllerIT : IntegrationTestBase() {
  @Autowired
  private lateinit var accommodationTypeRepository: AccommodationTypeRepository

  @Autowired
  private lateinit var accommodationStatusRepository: AccommodationStatusRepository

  @Autowired
  private lateinit var caseRepository: CaseRepository

  @Autowired
  private lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  private val cprAddressId = UUID.randomUUID()
  private lateinit var crn: String
  private lateinit var caseEntity: CaseEntity

  @BeforeEach
  fun setup() {
    proposedAccommodationRepository.deleteAll()
    crn = UUID.randomUUID().toString()
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })
    createTestDataSetupUserAndDeliusUser()
    HmppsAuthStubs.stubGrantToken()
  }

  @Test
  fun `should get current accommodation for crn`() {
    val corePersonRecord = buildCorePersonRecord(
      identifiers = buildIdentifiers(crns = listOf(crn)),
      addresses = listOf(
        buildCanonicalAddress(
          cprAddressId = UUID.randomUUID(),
          noFixedAbode = false,
          postcode = "W5 2AB",
          thoroughfareName = "Another Street",
          postTown = "London",
          startDate = LocalDate.of(2025, 10, 17),
          endDate = LocalDate.of(2026, 1, 10),
          status = CanonicalAddressStatus(
            code = AddressStatusCode.P.name,
            description = AddressStatusCode.P.description,
          ),
          usage = CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(
              code = AddressUsageCode.A07A.name,
              description = AddressUsageCode.A07A.description,
            ),
            isActive = true,
          ),
        ),
        buildCanonicalAddress(
          cprAddressId = UUID.randomUUID(),
          noFixedAbode = false,
          postcode = "SW1A 1AA",
          thoroughfareName = "Some Street",
          postTown = "London",
          startDate = LocalDate.of(2026, 1, 11),
          endDate = null,
          status = CanonicalAddressStatus(
            code = AddressStatusCode.M.name,
            description = AddressStatusCode.M.description,
          ),
          usage = CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(
              code = AddressUsageCode.A07B.name,
              description = AddressUsageCode.A07B.description,
            ),
            isActive = true,
          ),
        ),
      ),
    )
    CorePersonRecordStubs.getCorePersonRecordOKResponse(
      crn = crn,
      response = corePersonRecord,
    )
    restTestClient.get().uri("/cases/{crn}/accommodations/current", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCurrentAccommodationResponse(crn))
      }
  }

  @Test
  fun `get current accommodation should return partial success when CPR Addresses call returns server error`() {
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)
    restTestClient.get().uri("/cases/{crn}/accommodations/current", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCurrentAccommodationWithUpstreamFailureResponse())
      }
  }

  @Test
  fun `should get accommodation by id with ROLE_SINGLE_ACCOMMODATION_SERVICE__CORE_PERSON_RECORD role`() {
    val entity = createAndSaveProposedAccommodation()

    restTestClient.get().uri("/accommodations/{id}", entity.id)
      .withClientCredentialsJwt(
        roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE__CORE_PERSON_RECORD"),
      )
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetAccommodationByIdResponse(
            crn = crn,
            cprAddressId = cprAddressId,
            createdAt = entity.createdAt!!.atZone(ZoneId.systemDefault()).toLocalDate().toString(),
          ),
        )
      }
  }

  @Test
  fun `should return 404 when accommodation not found with ROLE_SINGLE_ACCOMMODATION_SERVICE__CORE_PERSON_RECORD role`() {
    val nonExistentId = UUID.randomUUID()

    restTestClient.get().uri("/accommodations/{id}", nonExistentId)
      .withDeliusUserJwt(roles = listOf("ROLE_SINGLE_ACCOMMODATION_SERVICE__CORE_PERSON_RECORD"))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 403 when get accommodation by id with Delius JWT`() {
    val entity = createAndSaveProposedAccommodation()

    restTestClient.get().uri("/accommodations/{id}", entity.id)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isForbidden
  }

  private fun createAndSaveProposedAccommodation(): ProposedAccommodationEntity {
    val accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue("A07B")
    val accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue("PR")
    val entity = buildProposedAccommodationEntity(
      caseId = caseEntity.id,
      cprAddressId = cprAddressId,
      accommodationTypeEntity = accommodationTypeEntity!!,
      accommodationStatusEntity = accommodationStatusEntity,
      subBuildingName = "test sub building name",
      buildingName = "test building name",
      buildingNumber = "4",
      throughfareName = "test thoroughfare",
      dependentLocality = "test dependent locality",
      postTown = "test post town",
      county = "test county",
      postcode = "test postcode",
      uprn = "test uprn",
    )
    return proposedAccommodationRepository.save(entity)
  }
}
