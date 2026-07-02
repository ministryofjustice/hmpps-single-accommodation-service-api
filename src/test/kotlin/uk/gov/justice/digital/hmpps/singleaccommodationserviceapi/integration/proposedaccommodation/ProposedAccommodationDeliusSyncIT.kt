package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation

import org.assertj.core.api.Assertions.assertThat
import org.javers.core.Javers
import org.javers.repository.jql.QueryBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.client.expectBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatusEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedGetProposedAccommodationsEmptyResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedGetProposedAccommodationsResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedProposedAccommodationTimeResponseForDeliusAndSasAudits
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedProposedAccommodationTimeResponseForDeliusOriginAudits
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.proposedAddressesRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.SasAndDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.OUTBOX_EVENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.PROPOSED_ACCOMMODATION
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus as EntityNextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus as EntityVerificationStatus

@TestPropertySource(properties = ["scheduling.enabled=true"])
class ProposedAccommodationDeliusSyncIT : IntegrationTestBase() {

  @Autowired
  private lateinit var accommodationTypeRepository: AccommodationTypeRepository

  @Autowired
  private lateinit var accommodationStatusRepository: AccommodationStatusRepository

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
    crn = UUID.randomUUID().toString()
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })

    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()
    createDeliusSyncUser()
    databaseUtils.truncate(PROPOSED_ACCOMMODATION, OUTBOX_EVENT)
  }

  @Test
  fun `should get proposed-accommodations by crn when there are two 'Unconfirmed' SAS Proposed Accommodations only - no sync required`() {
    val cprAccommodations = buildCorePersonRecord(
      identifiers = buildIdentifiers(crns = listOf(crn), prisonNumbers = listOf("PRI1")),
      addresses = emptyList(),
    )
    CorePersonRecordStubs.getCorePersonRecordOKResponse(
      crn = crn,
      response = cprAccommodations,
    )
    val olderEntityAccommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(code = AddressUsageCode.A07A.name)!!
    val olderEntity = createAndSaveProposedAccommodation(
      caseEntity = caseEntity,
      cprAddressId = null,
      accommodationSource = AccommodationSource.SAS,
      postcode = "RG26 5AG",
      buildingNumber = "4",
      thoroughfareName = "Dollis Green",
      postTown = "Bramley",
      country = null,
      startDate = null,
      verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
      nextAccommodationStatus = EntityNextAccommodationStatus.TO_BE_DECIDED,
      accommodationStatusEntity = null,
      accommodationTypeEntity = olderEntityAccommodationTypeEntity,
    )
    val newerEntityAccommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(code = AddressUsageCode.A07B.name)!!
    val newerEntity = createAndSaveProposedAccommodation(
      caseEntity = caseEntity,
      cprAddressId = UUID.randomUUID(),
      accommodationSource = AccommodationSource.SAS,
      postcode = "W1 8XX",
      buildingNumber = "11",
      thoroughfareName = "Piccadilly Circus",
      postTown = "London",
      country = null,
      verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
      nextAccommodationStatus = EntityNextAccommodationStatus.TO_BE_DECIDED,
      startDate = LocalDate.now(),
      accommodationStatusEntity = null,
      accommodationTypeEntity = newerEntityAccommodationTypeEntity,
    )

    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetProposedAccommodationsResponse(
            firstId = newerEntity.id,
            firstBuildingNumber = newerEntity.buildingNumber!!,
            firstCreatedBy = "Test Data Setup User",
            firstCreatedAt = newerEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
            firstAccommodationTypeEntity = newerEntityAccommodationTypeEntity,
            firstVerificationStatus = VerificationStatus.NOT_CHECKED_YET,
            firstNextAccommodationStatus = NextAccommodationStatus.TO_BE_DECIDED,
            firstStartDate = newerEntity.startDate,
            secondId = olderEntity.id,
            secondCreatedBy = "Test Data Setup User",
            secondCreatedAt = olderEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
            secondAccommodationTypeEntity = olderEntityAccommodationTypeEntity,
            secondVerificationStatus = VerificationStatus.NOT_CHECKED_YET,
            secondNextAccommodationStatus = NextAccommodationStatus.TO_BE_DECIDED,
            secondStartDate = olderEntity.startDate,
            crn = crn,
          ),
        )
      }
  }

  @Test
  fun `should NOT insert Delius origin 'Main' accommodation record as should only insert Delius origin 'Proposed' accommodation records`() {
    val crn = "ABCDEFG"
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })

    mockCurrentPrisonAccommodationAndDeliusOriginAccommodation(
      crn,
      deliusOriginAddressStatusCode = AddressStatusCode.M,
      deliusProposedAccommodationBuildingNumber = "Delius buildingName",
      deliusOriginProposedAccommodationTypeCode = AddressUsageCode.A07A,
      deliusOriginProposedAccommodationStartDate = LocalDate.now().minusDays(10),
      deliusOriginProposedAccommodationEndDate = LocalDate.now().minusDays(5),
    )

    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult()
      .responseBody!!

    val results = proposedAccommodationRepository.findAll()
    assertThat(results).hasSize(0)
  }

  @Test
  fun `should insert Delius origin 'Proposed' accommodation record with all data when does not exist in SAS database`() {
    val crn = "ABCDEFG"
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })
    shouldInsertDeliusOriginRecordWhenDoesNotExistInSasDb(
      crn,
      deliusProposedAccommodationBuildingNumber = "Delius buildingName",
      deliusOriginProposedAccommodationTypeCode = AddressUsageCode.A07A,
      deliusOriginProposedAccommodationStatusCode = AddressStatusCode.PR,
      deliusOriginProposedAccommodationStartDate = LocalDate.now().minusDays(10),
      deliusOriginProposedAccommodationEndDate = LocalDate.now().plusDays(5),
    )
  }

  @Test
  fun `should insert Delius origin 'Proposed' accommodation record with all data when record does not exist in SAS database and SAS case record does not exist in database either`() {
    val crn = "ABCDEFG"
    val prisonNumber = "PRI1"
    val case = buildCase(crn, nomsNumber = prisonNumber)
    SasAndDeliusStubs.stubGetCase(deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER, crn, response = case)

    shouldInsertDeliusOriginRecordWhenDoesNotExistInSasDb(
      crn,
      deliusProposedAccommodationBuildingNumber = "11",
      deliusOriginProposedAccommodationTypeCode = AddressUsageCode.A07A,
      deliusOriginProposedAccommodationStatusCode = AddressStatusCode.PR1,
      deliusOriginProposedAccommodationStartDate = LocalDate.now().minusDays(10),
      deliusOriginProposedAccommodationEndDate = LocalDate.now().plusDays(5),
    )

    val newCaseInserted = caseRepository.findByCrn(crn)
    val newCaseCrnIdentifier = newCaseInserted?.caseIdentifiers
      ?.firstOrNull { it.identifierType == IdentifierType.CRN }
    val newCasePrisonNumberIdentifier = newCaseInserted?.caseIdentifiers
      ?.firstOrNull { it.identifierType == IdentifierType.PRISON_NUMBER }

    assertThat(newCaseInserted).isNotNull
    assertThat(newCaseCrnIdentifier).isNotNull
    assertThat(newCasePrisonNumberIdentifier).isNotNull
    assertThat(newCaseCrnIdentifier!!.identifier).isEqualTo(crn)
    assertThat(newCasePrisonNumberIdentifier!!.identifier).isEqualTo(prisonNumber)
  }

  @Test
  fun `should return Server error and NOT insert Delius origin 'Proposed' accommodation record when retrieving the case from Delius fails`() {
    val crn = "ABCDEFG"
    SasAndDeliusStubs.stubGetCaseFailure(deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER, crn)
    mockCurrentPrisonAccommodationAndDeliusOriginAccommodation(
      crn,
      deliusOriginAddressStatusCode = AddressStatusCode.PR1,
      deliusOriginProposedAccommodationTypeCode = AddressUsageCode.A07A,
      deliusOriginProposedAccommodationStartDate = LocalDate.now().minusDays(10),
      deliusOriginProposedAccommodationEndDate = LocalDate.now().minusDays(5),
    )

    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus()
      .is5xxServerError

    assertThat(caseRepository.findByCrn(crn)).isNull()
    assertThat(proposedAccommodationRepository.findAll().isEmpty())
  }

  @Test
  fun `should NOT insert Delius origin 'Proposed' accommodation record when the accommodation type is a non-Probation type`() {
    val crn = "ABCDEFG"
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })
    mockCurrentPrisonAccommodationAndDeliusOriginAccommodation(
      crn,
      deliusOriginAddressStatusCode = AddressStatusCode.PR1,
      deliusOriginProposedAccommodationTypeCode = AddressUsageCode.HOSP,
      deliusOriginProposedAccommodationStartDate = LocalDate.now().minusDays(10),
      deliusOriginProposedAccommodationEndDate = LocalDate.now().minusDays(5),
    )
    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult()
      .responseBody!!

    val results = proposedAccommodationRepository.findAll()
    assertThat(results).hasSize(0)
  }

  @Test
  fun `should insert Delius origin 'Proposed' accommodation record when record does not have an accommodation type`() {
    val crn = "ABCDEFG"
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })
    shouldInsertDeliusOriginRecordWhenDoesNotExistInSasDb(
      crn,
      deliusProposedAccommodationBuildingNumber = "11",
      deliusOriginProposedAccommodationTypeCode = null,
      deliusOriginProposedAccommodationStatusCode = AddressStatusCode.PR1,
      deliusOriginProposedAccommodationStartDate = LocalDate.now().minusDays(10),
      deliusOriginProposedAccommodationEndDate = LocalDate.now().plusDays(5),
    )
  }

  @Test
  fun `should sync all data on SAS 'Proposed' accommodation record when someone has changed all the data on the 'Proposed' accommodation record in nDelius`() {
    val results = shouldSyncAllDataInSASAccommodationRecordWhenSomeoneHasChangedAllDataOnTheDeliusAccommodationRecord(
      deliusRecordAddressStatusCode = AddressStatusCode.PR1,
    )
    val response: String = results[0] as String
    val updatedRecord = results[1] as ProposedAccommodationEntity
    val updatedAccommodationTypeEntity = results[2] as AccommodationTypeEntity?
    val updatedAccommodationStatusEntity = results[3] as AccommodationStatusEntity?

    assertThatJson(response).matchesExpectedJson(
      expectedGetProposedAccommodationsResponse(
        expectedId = updatedRecord.id,
        expectedPostcode = updatedRecord.postcode!!,
        expectedSubBuildingName = updatedRecord.subBuildingName!!,
        expectedBuildingName = updatedRecord.buildingName!!,
        expectedBuildingNumber = updatedRecord.buildingNumber!!,
        expectedThoroughfareName = updatedRecord.throughfareName!!,
        expectedDependentLocality = updatedRecord.dependentLocality!!,
        expectedPostTown = updatedRecord.postTown!!,
        expectedCounty = updatedRecord.county!!,
        expectedUprn = updatedRecord.uprn!!,
        expectedAccommodationTypeEntity = accommodationTypeRepository.findByIdOrNull(updatedAccommodationTypeEntity!!.id)!!,
        expectedAccommodationStatusEntity = accommodationStatusRepository.findByIdOrNull(updatedAccommodationStatusEntity!!.id)!!,
        expectedVerificationStatus = VerificationStatus.PASSED,
        expectedNextAccommodationStatus = NextAccommodationStatus.YES,
        expectedStartDate = updatedRecord.startDate!!,
        expectedEndDate = updatedRecord.endDate!!,
        expectedCreatedAt = updatedRecord.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        expectedCreatedBy = "Test Data Setup User",
        crn = crn,
      ),
    )
  }

  @Test
  fun `should sync all data on SAS 'Proposed' accommodation record when someone updates accommodation type to null`() {
    val results = shouldSyncAllDataInSASAccommodationRecordWhenSomeoneHasChangedAllDataOnTheDeliusAccommodationRecord(
      deliusRecordAddressStatusCode = AddressStatusCode.PR,
      deliusRecordAccommodationTypeCode = null,
    )
    val updatedRecord = results[1] as ProposedAccommodationEntity?

    assertThat(updatedRecord!!.accommodationTypeId).isNull()
  }

  @Test
  fun `should sync all data on SAS 'Proposed' accommodation record when someone has transitioned the 'Proposed' accommodation record in nDelius to be the 'Main' accommodation record`() {
    val results = shouldSyncAllDataInSASAccommodationRecordWhenSomeoneHasChangedAllDataOnTheDeliusAccommodationRecord(
      deliusRecordAddressStatusCode = AddressStatusCode.M,
    )
    val response: String = results[0] as String
    assertThatJson(response).matchesExpectedJson(
      expectedGetProposedAccommodationsEmptyResponse(),
    )
  }

  private fun shouldSyncAllDataInSASAccommodationRecordWhenSomeoneHasChangedAllDataOnTheDeliusAccommodationRecord(
    deliusRecordAddressStatusCode: AddressStatusCode,
    deliusRecordAccommodationTypeCode: AddressUsageCode? = AddressUsageCode.A07A,
  ): List<Any?> {
    val startDate = LocalDate.now().minusDays(10)
    val commonCprAddressId = UUID.randomUUID()
    val sasOriginProposedAccommodationEntity = buildProposedAccommodationEntity(
      caseId = caseEntity.id,
      cprAddressId = commonCprAddressId,
      accommodationSource = AccommodationSource.SAS,
      name = null,
      noFixedAbode = false,
      typeVerified = false,
      startDate = startDate,
      endDate = null,
      postcode = "Original postcode",
      subBuildingName = "Original subBuildingName",
      buildingName = "Original buildingName",
      buildingNumber = "Original buildingNumber",
      throughfareName = "Original thoroughfareName",
      dependentLocality = "Original dependentLocality",
      postTown = "Original postTown",
      county = "Original county",
      country = "Original country",
      uprn = "Original uprn",
      accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue(AddressStatusCode.PR.name)!!,
      accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(AddressUsageCode.A07B.name)!!,
      verificationStatus = EntityVerificationStatus.PASSED,
      nextAccommodationStatus = EntityNextAccommodationStatus.YES,
    )
    proposedAccommodationRepository.save(sasOriginProposedAccommodationEntity)

    val updatedStartDate = startDate.minusDays(10)
    val updatedEndDate = updatedStartDate.plusDays(22)
    val equivalentRecordInDeliusWithUpdatesOnAllFields = buildCanonicalAddress(
      cprAddressId = commonCprAddressId,
      noFixedAbode = true,
      typeVerified = true,
      startDate = updatedStartDate,
      endDate = updatedEndDate,
      postcode = "Updated postcode",
      subBuildingName = "Updated subBuildingName",
      buildingName = "Updated buildingName",
      buildingNumber = "Updated buildingNumber",
      thoroughfareName = "Updated thoroughfareName",
      dependentLocality = "Updated dependentLocality",
      postTown = "Updated postTown",
      county = "Updated county",
      country = "Updated country",
      uprn = "Updated uprn",
      status = CanonicalAddressStatus(
        code = deliusRecordAddressStatusCode.name,
        description = deliusRecordAddressStatusCode.description,
      ),
      usages = deliusRecordAccommodationTypeCode?.let {
        listOf(
          CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(
              code = AddressUsageCode.A07A.name,
              description = AddressUsageCode.A07A.description,
            ),
            isActive = true,
          ),
        )
      } ?: emptyList(),
    )
    val cprAccommodationsWithDeliusUpdates = buildCorePersonRecord(
      identifiers = buildIdentifiers(crns = listOf(crn)),
      addresses = listOf(
        equivalentRecordInDeliusWithUpdatesOnAllFields,
      ),
    )
    CorePersonRecordStubs.getCorePersonRecordOKResponse(
      crn = crn,
      response = cprAccommodationsWithDeliusUpdates,
    )

    val response: String = restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult()
      .responseBody!!

    val results = proposedAccommodationRepository.findAll()
    assertThat(results).hasSize(1)

    val updatedRecord = results.firstOrNull { it.accommodationSource == AccommodationSource.SAS }
    val updatedAccommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue(deliusRecordAddressStatusCode.name)!!
    val updatedAccommodationTypeEntity = deliusRecordAccommodationTypeCode?.let {
      accommodationTypeRepository.findByCodeAndActiveIsTrue(AddressUsageCode.A07A.name)!!
    }

    assertThat(updatedRecord).isNotNull
    assertThat(updatedRecord!!.cprAddressId).isEqualTo(commonCprAddressId)
    assertThat(updatedRecord.accommodationSource).isEqualTo(AccommodationSource.SAS)
    assertThat(updatedRecord.name).isNull()
    assertThat(updatedRecord.accommodationTypeId).isEqualTo(updatedAccommodationTypeEntity?.id)
    assertThat(updatedRecord.accommodationStatusId).isEqualTo(updatedAccommodationStatusEntity.id)
    assertThat(updatedRecord.verificationStatus).isEqualTo(EntityVerificationStatus.PASSED)
    assertThat(updatedRecord.nextAccommodationStatus).isEqualTo(EntityNextAccommodationStatus.YES)
    assertThat(updatedRecord.typeVerified).isTrue()
    assertThat(updatedRecord.noFixedAbode).isTrue()
    assertThat(updatedRecord.postcode).isEqualTo(equivalentRecordInDeliusWithUpdatesOnAllFields.postcode)
    assertThat(updatedRecord.subBuildingName).isEqualTo(equivalentRecordInDeliusWithUpdatesOnAllFields.subBuildingName)
    assertThat(updatedRecord.buildingName).isEqualTo(equivalentRecordInDeliusWithUpdatesOnAllFields.buildingName)
    assertThat(updatedRecord.buildingNumber).isEqualTo(equivalentRecordInDeliusWithUpdatesOnAllFields.buildingNumber)
    assertThat(updatedRecord.throughfareName).isEqualTo(equivalentRecordInDeliusWithUpdatesOnAllFields.thoroughfareName)
    assertThat(updatedRecord.dependentLocality).isEqualTo(equivalentRecordInDeliusWithUpdatesOnAllFields.dependentLocality)
    assertThat(updatedRecord.postTown).isEqualTo(equivalentRecordInDeliusWithUpdatesOnAllFields.postTown)
    assertThat(updatedRecord.county).isEqualTo(equivalentRecordInDeliusWithUpdatesOnAllFields.county)
    assertThat(updatedRecord.country).isNull()
    assertThat(updatedRecord.uprn).isEqualTo(equivalentRecordInDeliusWithUpdatesOnAllFields.uprn)
    assertThat(updatedRecord.startDate).isEqualTo(updatedStartDate)
    assertThat(updatedRecord.endDate).isEqualTo(updatedEndDate)
    assertThat(updatedRecord.createdByUserId).isEqualTo(userIdOfTestDataSetupUser)
    assertThat(updatedRecord.createdAt).isBetween(
      beforeTest.minusSeconds(1),
      Instant.now().plusSeconds(1),
    )
    assertThat(outboxEventRepository.findAll()).isEmpty()
    return listOf(response, updatedRecord, updatedAccommodationTypeEntity, updatedAccommodationStatusEntity)
  }

  @Test
  fun `should sync SAS record when there are some further updates in Delius even after SAS updates the record`() {
    val crn = "ABCDEFG"
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })
    val (deliusSyncedRecord, deliusOriginProposedAccommodation) = shouldInsertUnknownDeliusOriginRecordAndThenSyncFurtherUpdate(crn)
    val originalDeliusSyncBuildingNumber = "15"
    assertThat(deliusSyncedRecord.buildingNumber).isEqualTo(originalDeliusSyncBuildingNumber)

    val sasUpdatedBuildingNumber = "100"
    restTestClient.put().uri("/cases/$crn/proposed-accommodations/${deliusSyncedRecord.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          accommodationTypeCode = accommodationTypeRepository.findByIdOrNull(deliusSyncedRecord.accommodationTypeId!!)!!.code,
          accommodationStatusCode = accommodationStatusRepository.findByIdOrNull(deliusSyncedRecord.accommodationStatusId!!)!!.code,
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
          subBuildingName = deliusSyncedRecord.subBuildingName,
          buildingName = deliusSyncedRecord.buildingName,
          buildingNumber = sasUpdatedBuildingNumber,
          thoroughfareName = deliusSyncedRecord.throughfareName,
          dependentLocality = deliusSyncedRecord.dependentLocality,
          postTown = deliusSyncedRecord.postTown,
          county = deliusSyncedRecord.county,
          country = deliusSyncedRecord.country,
          postcode = deliusSyncedRecord.postcode!!,
          startDate = deliusSyncedRecord.startDate?.toString(),
          endDate = deliusSyncedRecord.endDate?.toString(),
          uprn = deliusSyncedRecord.uprn,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    var results = proposedAccommodationRepository.findAll()
    assertThat(results).hasSize(1)

    var updatedRecord = results.firstOrNull { it.accommodationSource == AccommodationSource.DELIUS }
    assertThat(updatedRecord).isNotNull
    assertThat(updatedRecord!!.buildingNumber).isEqualTo(sasUpdatedBuildingNumber)
    assertThat(updatedRecord.subBuildingName).isEqualTo(deliusSyncedRecord.subBuildingName)

    // mock new call to CPR which gives us a change made in nDelius for same record (different building number in address)
    val latestDeliusUpdatedBuildingNumber = "200"
    val deliusOriginProposedAccommodationCopyWithDifferentBuildingNumber = deliusOriginProposedAccommodation.copy(
      buildingNumber = latestDeliusUpdatedBuildingNumber,
    )
    val cprAccommodations = buildCorePersonRecord(
      identifiers = buildIdentifiers(crns = listOf(crn)),
      addresses = listOf(
        deliusOriginProposedAccommodationCopyWithDifferentBuildingNumber,
      ),
    )
    CorePersonRecordStubs.getCorePersonRecordOKResponse(
      crn = crn,
      response = cprAccommodations,
    )

    // get proposed-accommodations and ensure we get the latest Delius update
    val response: String = restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult()
      .responseBody!!

    results = proposedAccommodationRepository.findAll()
    assertThat(results).hasSize(1)

    updatedRecord = results.firstOrNull { it.accommodationSource == AccommodationSource.DELIUS }
    assertThat(updatedRecord).isNotNull
    assertThat(updatedRecord!!.buildingNumber).isEqualTo(latestDeliusUpdatedBuildingNumber)

    assertThatJson(response).matchesExpectedJson(
      expectedGetProposedAccommodationsResponse(
        expectedId = deliusSyncedRecord.id,
        expectedPostcode = deliusOriginProposedAccommodation.postcode!!,
        expectedSubBuildingName = deliusOriginProposedAccommodation.subBuildingName!!,
        expectedBuildingName = deliusOriginProposedAccommodation.buildingName!!,
        expectedBuildingNumber = latestDeliusUpdatedBuildingNumber,
        expectedThoroughfareName = deliusOriginProposedAccommodation.thoroughfareName!!,
        expectedDependentLocality = deliusOriginProposedAccommodation.dependentLocality!!,
        expectedPostTown = deliusOriginProposedAccommodation.postTown!!,
        expectedCounty = deliusOriginProposedAccommodation.county!!,
        expectedUprn = deliusOriginProposedAccommodation.uprn!!,
        expectedAccommodationTypeEntity = accommodationTypeRepository.findByIdOrNull(updatedRecord.accommodationTypeId!!)!!,
        expectedAccommodationStatusEntity = accommodationStatusRepository.findByIdOrNull(updatedRecord.accommodationStatusId!!)!!,
        expectedVerificationStatus = VerificationStatus.PASSED,
        expectedNextAccommodationStatus = NextAccommodationStatus.YES,
        expectedStartDate = deliusSyncedRecord.startDate!!,
        expectedEndDate = deliusSyncedRecord.endDate!!,
        expectedCreatedAt = updatedRecord.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        expectedCreatedBy = "nDelius user",
        crn = crn,
      ),
    )
    assertThat(outboxEventRepository.findAll().size).isEqualTo(1)
  }

  private fun shouldInsertDeliusOriginRecordWhenDoesNotExistInSasDb(
    crn: String,
    deliusProposedAccommodationBuildingNumber: String,
    deliusOriginProposedAccommodationTypeCode: AddressUsageCode?,
    deliusOriginProposedAccommodationStatusCode: AddressStatusCode,
    deliusOriginProposedAccommodationStartDate: LocalDate,
    deliusOriginProposedAccommodationEndDate: LocalDate,
  ): Pair<CanonicalAddress, CanonicalAddress> {
    val (currentPrisonAccommodation, deliusOriginProposedAccommodation) = mockCurrentPrisonAccommodationAndDeliusOriginAccommodation(
      crn,
      deliusOriginAddressStatusCode = deliusOriginProposedAccommodationStatusCode,
      deliusOriginProposedAccommodationTypeCode,
      deliusOriginProposedAccommodationStartDate,
      deliusOriginProposedAccommodationEndDate,
      deliusProposedAccommodationBuildingNumber,
    )

    val response: String = restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult()
      .responseBody!!

    val results = proposedAccommodationRepository.findAll()
    assertThat(results).hasSize(1)

    val deliusSyncedRecord = results.firstOrNull { it.accommodationSource == AccommodationSource.DELIUS }
    assertThat(deliusSyncedRecord).isNotNull

    assertThatJson(response).matchesExpectedJson(
      expectedGetProposedAccommodationsResponse(
        expectedId = deliusSyncedRecord!!.id,
        expectedPostcode = deliusOriginProposedAccommodation.postcode!!,
        expectedSubBuildingName = deliusOriginProposedAccommodation.subBuildingName!!,
        expectedBuildingName = deliusOriginProposedAccommodation.buildingName!!,
        expectedBuildingNumber = deliusOriginProposedAccommodation.buildingNumber!!,
        expectedThoroughfareName = deliusOriginProposedAccommodation.thoroughfareName!!,
        expectedDependentLocality = deliusOriginProposedAccommodation.dependentLocality!!,
        expectedPostTown = deliusOriginProposedAccommodation.postTown!!,
        expectedCounty = deliusOriginProposedAccommodation.county!!,
        expectedUprn = deliusOriginProposedAccommodation.uprn!!,
        expectedAccommodationTypeEntity = deliusOriginProposedAccommodationTypeCode?.let {
          accommodationTypeRepository.findByCodeAndActiveIsTrue(it.name)
        },
        expectedAccommodationStatusEntity = deliusOriginProposedAccommodationStatusCode?.let {
          accommodationStatusRepository.findByCodeAndActiveIsTrue(it.name)
        },
        expectedVerificationStatus = VerificationStatus.PASSED,
        expectedNextAccommodationStatus = NextAccommodationStatus.YES,
        expectedStartDate = deliusOriginProposedAccommodationStartDate,
        expectedEndDate = deliusOriginProposedAccommodationEndDate,
        expectedCreatedAt = deliusSyncedRecord.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        expectedCreatedBy = "nDelius user",
        crn = crn,
      ),
    )
    return currentPrisonAccommodation to deliusOriginProposedAccommodation
  }

  private fun mockCurrentPrisonAccommodationAndDeliusOriginAccommodation(
    crn: String,
    deliusOriginAddressStatusCode: AddressStatusCode,
    deliusOriginProposedAccommodationTypeCode: AddressUsageCode?,
    deliusOriginProposedAccommodationStartDate: LocalDate,
    deliusOriginProposedAccommodationEndDate: LocalDate,
    deliusProposedAccommodationBuildingNumber: String = "Delius buildingName",
  ): Pair<CanonicalAddress, CanonicalAddress> {
    val deliusOriginProposedAccommodation = buildCanonicalAddress(
      cprAddressId = UUID.randomUUID(),
      noFixedAbode = false,
      typeVerified = false,
      postcode = "Delius postcode",
      subBuildingName = "Delius subBuildingName",
      buildingName = "Delius buildingName",
      buildingNumber = deliusProposedAccommodationBuildingNumber,
      thoroughfareName = "Delius thoroughfareName",
      dependentLocality = "Delius dependentLocality",
      postTown = "Delius postTown",
      county = "Delius county",
      country = "Delius country",
      uprn = "Delius uprn",
      startDate = deliusOriginProposedAccommodationStartDate,
      endDate = deliusOriginProposedAccommodationEndDate,
      status = CanonicalAddressStatus(
        code = deliusOriginAddressStatusCode.name,
        description = deliusOriginAddressStatusCode.description,
      ),
      usages = listOf(
        CanonicalAddressUsage(
          usageCode = CanonicalAddressUsageCode(
            code = deliusOriginProposedAccommodationTypeCode?.name,
            description = deliusOriginProposedAccommodationTypeCode?.description,
          ),
          isActive = true,
        ),
      ),
    )
    val currentPrisonAccommodation = buildCanonicalAddress(
      cprAddressId = UUID.randomUUID(),
      typeVerified = true,
      noFixedAbode = true,
      buildingName = "Bullingdon HMP",
      postcode = null,
      subBuildingName = null,
      buildingNumber = null,
      thoroughfareName = null,
      dependentLocality = null,
      postTown = null,
      county = null,
      country = null,
      countryCode = null,
      startDate = LocalDate.now().minusYears(5),
      endDate = null,
      status = CanonicalAddressStatus(
        code = AddressStatusCode.M.name,
        description = AddressStatusCode.M.description,
      ),
      usages = listOf(
        CanonicalAddressUsage(
          usageCode = CanonicalAddressUsageCode(
            code = "HMP",
            description = "Prison",
          ),
          isActive = true,
        ),
      ),
    )
    val cprAccommodations = buildCorePersonRecord(
      identifiers = buildIdentifiers(crns = listOf(crn), prisonNumbers = listOf("PRI1")),
      addresses = listOf(
        deliusOriginProposedAccommodation,
        currentPrisonAccommodation,
      ),
    )
    CorePersonRecordStubs.getCorePersonRecordOKResponse(
      crn = crn,
      response = cprAccommodations,
    )
    return currentPrisonAccommodation to deliusOriginProposedAccommodation
  }

  fun shouldInsertUnknownDeliusOriginRecordAndThenSyncFurtherUpdate(crn: String): Pair<ProposedAccommodationEntity, CanonicalAddress> {
    val deliusOriginProposedAccommodationTypeCode = AddressUsageCode.A07A
    val deliusOriginProposedAccommodationStatusCode = AddressStatusCode.PR1
    val deliusOriginProposedAccommodationStartDate = LocalDate.now().minusDays(10)
    val deliusOriginProposedAccommodationEndDate = LocalDate.now().plusDays(5)
    val originalBuildingNumberInDelius = "11"
    val updatedBuildingNumberInDelius = "15"

    // steps to insert "Delius origin" record into the SAS database
    val (currentPrisonAccommodation, deliusOriginProposedAccommodation) = shouldInsertDeliusOriginRecordWhenDoesNotExistInSasDb(
      crn,
      deliusProposedAccommodationBuildingNumber = originalBuildingNumberInDelius,
      deliusOriginProposedAccommodationTypeCode = deliusOriginProposedAccommodationTypeCode,
      deliusOriginProposedAccommodationStatusCode = deliusOriginProposedAccommodationStatusCode,
      deliusOriginProposedAccommodationStartDate = deliusOriginProposedAccommodationStartDate,
      deliusOriginProposedAccommodationEndDate = deliusOriginProposedAccommodationEndDate,
    )

    var results = proposedAccommodationRepository.findAll()
    assertThat(results).hasSize(1)

    var deliusSyncedRecord = results.firstOrNull { it.accommodationSource == AccommodationSource.DELIUS }
    assertThat(deliusSyncedRecord).isNotNull
    assertThat(deliusSyncedRecord!!.buildingNumber).isEqualTo(originalBuildingNumberInDelius)

    // mock new call to CPR which gives us a change made in nDelius for same "Delius origin" record (different building number in address)
    val deliusOriginProposedAccommodationCopyWithDifferentBuildingNumber = deliusOriginProposedAccommodation.copy(
      buildingNumber = updatedBuildingNumberInDelius,
    )
    val cprAccommodations = buildCorePersonRecord(
      identifiers = buildIdentifiers(crns = listOf(crn)),
      addresses = listOf(
        deliusOriginProposedAccommodationCopyWithDifferentBuildingNumber,
        currentPrisonAccommodation,
      ),
    )
    CorePersonRecordStubs.getCorePersonRecordOKResponse(
      crn = crn,
      response = cprAccommodations,
    )

    // get proposed-accommodations and ensure the latest Delius updated is synchronised to our original db record and the correct response is returned with the change
    val response: String = restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult()
      .responseBody!!

    results = proposedAccommodationRepository.findAll()
    assertThat(results).hasSize(1)

    deliusSyncedRecord = results.firstOrNull { it.accommodationSource == AccommodationSource.DELIUS }
    assertThat(deliusSyncedRecord).isNotNull
    assertThat(deliusSyncedRecord!!.buildingNumber).isEqualTo(updatedBuildingNumberInDelius)

    assertThatJson(response).matchesExpectedJson(
      expectedGetProposedAccommodationsResponse(
        expectedId = deliusSyncedRecord.id,
        expectedPostcode = deliusOriginProposedAccommodation.postcode!!,
        expectedSubBuildingName = deliusOriginProposedAccommodation.subBuildingName!!,
        expectedBuildingName = deliusOriginProposedAccommodation.buildingName!!,
        expectedBuildingNumber = updatedBuildingNumberInDelius,
        expectedThoroughfareName = deliusOriginProposedAccommodation.thoroughfareName!!,
        expectedDependentLocality = deliusOriginProposedAccommodation.dependentLocality!!,
        expectedPostTown = deliusOriginProposedAccommodation.postTown!!,
        expectedCounty = deliusOriginProposedAccommodation.county!!,
        expectedUprn = deliusOriginProposedAccommodation.uprn!!,
        expectedAccommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(deliusOriginProposedAccommodationTypeCode.name)!!,
        expectedAccommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue(deliusOriginProposedAccommodationStatusCode.name)!!,
        expectedVerificationStatus = VerificationStatus.PASSED,
        expectedNextAccommodationStatus = NextAccommodationStatus.YES,
        expectedStartDate = deliusOriginProposedAccommodationStartDate,
        expectedEndDate = deliusOriginProposedAccommodationEndDate,
        expectedCreatedAt = deliusSyncedRecord.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        expectedCreatedBy = "nDelius user",
        crn = crn,
      ),
    )
    assertThat(outboxEventRepository.findAll()).isEmpty()
    return Pair(deliusSyncedRecord, deliusOriginProposedAccommodation)
  }

//  @Test
//  fun `should delete the correct SAS accommodation record when it has been deleted in nDelius`() {
//    val crn = "ABCDEFG"
//    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })
//
//    val commonCprAddressId = UUID.randomUUID()
//    val addressForRecordInSasAndInDelius = buildCanonicalAddress(
//      postcode = "RG26 5AG",
//      buildingNumber = "4",
//      thoroughfareName = "Dollis Green",
//      postTown = "Bramley",
//      country = null,
//    )
//    val firstPreExistingConfirmedProposedAccommodationType = accommodationTypeRepository.findByCodeAndActiveIsTrue(code = AddressUsageCode.A07A.name)!!
//    val firstPreExistingConfirmedProposedAccommodationEntity = createAndSaveProposedAccommodation(
//      caseEntity = caseEntity,
//      cprAddressId = commonCprAddressId,
//      accommodationSource = AccommodationSource.SAS,
//      postcode = addressForRecordInSasAndInDelius.postcode!!,
//      buildingNumber = addressForRecordInSasAndInDelius.buildingNumber!!,
//      thoroughfareName = addressForRecordInSasAndInDelius.thoroughfareName!!,
//      postTown = addressForRecordInSasAndInDelius.postTown!!,
//      country = null,
//      startDate = LocalDate.now().minusDays(1),
//      verificationStatus = EntityVerificationStatus.PASSED,
//      nextAccommodationStatus = EntityNextAccommodationStatus.YES,
//      accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue(code = AddressStatusCode.PR.name),
//      accommodationTypeEntity = firstPreExistingConfirmedProposedAccommodationType,
//    )
//    firstPreExistingConfirmedProposedAccommodationEntity.createdAt = ZonedDateTime.now().minusSeconds(11).toInstant()
//    proposedAccommodationRepository.save(firstPreExistingConfirmedProposedAccommodationEntity)
//
//    val secondPreExistingConfirmedProposedAccommodationType = createAndSaveProposedAccommodation(
//      caseEntity = caseEntity,
//      cprAddressId = UUID.randomUUID(),
//      accommodationSource = AccommodationSource.SAS,
//      postcode = "W3 9XE",
//      buildingNumber = "511",
//      thoroughfareName = "Test street",
//      postTown = "London",
//      country = "England",
//      startDate = null,
//      verificationStatus = EntityVerificationStatus.PASSED,
//      nextAccommodationStatus = EntityNextAccommodationStatus.YES,
//      accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue(code = AddressStatusCode.PR.name),
//      accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(code = AddressUsageCode.A07B.name)!!,
//    )
//    secondPreExistingConfirmedProposedAccommodationType.createdAt = ZonedDateTime.now().minusSeconds(11).toInstant()
//    proposedAccommodationRepository.save(secondPreExistingConfirmedProposedAccommodationType)
//
//    val preExistingUnconfirmedAccommodationType = accommodationTypeRepository.findByCodeAndActiveIsTrue(code = AddressUsageCode.A01A.name)!!
//    val preExistingUnconfirmedProposedAccommodationEntity = createAndSaveProposedAccommodation(
//      caseEntity = caseEntity,
//      cprAddressId = null,
//      accommodationSource = AccommodationSource.SAS,
//      postcode = "W1 8XX",
//      buildingNumber = "11",
//      thoroughfareName = "Piccadilly Circus",
//      postTown = "London",
//      country = null,
//      verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
//      nextAccommodationStatus = EntityNextAccommodationStatus.TO_BE_DECIDED,
//      startDate = LocalDate.now(),
//      accommodationStatusEntity = null,
//      accommodationTypeEntity = preExistingUnconfirmedAccommodationType,
//    )
//    preExistingUnconfirmedProposedAccommodationEntity.createdAt = ZonedDateTime.now().minusSeconds(11).toInstant()
//    proposedAccommodationRepository.save(preExistingUnconfirmedProposedAccommodationEntity)
//
//    val deliusOriginProposedAccommodation = buildCanonicalAddress(
//      cprAddressId = commonCprAddressId,
//      noFixedAbode = false,
//      typeVerified = false,
//
//      postcode = addressForRecordInSasAndInDelius.postcode!!,
//      buildingNumber = addressForRecordInSasAndInDelius.buildingNumber!!,
//      thoroughfareName = addressForRecordInSasAndInDelius.thoroughfareName!!,
//      postTown = addressForRecordInSasAndInDelius.postTown!!,
//      startDate = LocalDate.now().minusDays(1),
//      endDate = null,
//      status = CanonicalAddressStatus(
//        code = AddressStatusCode.PR.name,
//        description = AddressStatusCode.PR.description,
//      ),
//      usage = CanonicalAddressUsage(
//        usageCode = CanonicalAddressUsageCode(
//          code = AddressUsageCode.A07A.name,
//          description = AddressUsageCode.A07A.description,
//        ),
//        isActive = true,
//      ),
//    )
//    CorePersonRecordStubs.getCorePersonRecordOKResponse(
//      crn = crn,
//      response = buildCorePersonRecord(
//        identifiers = buildIdentifiers(crns = listOf(crn), prisonNumbers = listOf("PRI1")),
//        addresses = listOf(deliusOriginProposedAccommodation),
//      ),
//    )
//
//    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
//      .withDeliusUserJwt()
//      .exchangeSuccessfully()
//      .expectBody<String>()
//      .value {
//        assertThatJson(it!!).matchesExpectedJson(
//          expectedGetProposedAccommodationsResponse(
//            firstId = preExistingUnconfirmedProposedAccommodationEntity.id,
//            firstBuildingNumber = preExistingUnconfirmedProposedAccommodationEntity.buildingNumber!!,
//            firstCreatedBy = "Test Data Setup User",
//            firstCreatedAt = preExistingUnconfirmedProposedAccommodationEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
//            firstAccommodationTypeEntity = preExistingUnconfirmedAccommodationType,
//            firstVerificationStatus = VerificationStatus.NOT_CHECKED_YET,
//            firstNextAccommodationStatus = NextAccommodationStatus.TO_BE_DECIDED,
//            firstStartDate = preExistingUnconfirmedProposedAccommodationEntity.startDate,
//            secondId = firstPreExistingConfirmedProposedAccommodationEntity.id,
//            secondCreatedBy = "Test Data Setup User",
//            secondCreatedAt = firstPreExistingConfirmedProposedAccommodationEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
//            secondAccommodationTypeEntity = firstPreExistingConfirmedProposedAccommodationType,
//            secondVerificationStatus = VerificationStatus.PASSED,
//            secondNextAccommodationStatus = NextAccommodationStatus.YES,
//            secondStartDate = firstPreExistingConfirmedProposedAccommodationEntity.startDate,
//            crn = crn,
//          ),
//        )
//      }
//
//    val results = proposedAccommodationRepository.findAll()
//    assertThat(results).hasSize(3)
//
//    val softDeletedRecords = results.filter { it.deleted }
//    assertThat(softDeletedRecords).hasSize(1)
//    assertThat(softDeletedRecords.first().id).isEqualTo(secondPreExistingConfirmedProposedAccommodationType.id)
//
//    assertThat(outboxEventRepository.findAll().size).isEqualTo(0)
//  }
//
//  @Test
//  fun `should delete the correct SAS accommodation record when no accommodation records in nDelius`() {
//    val crn = "ABCDEFG"
//    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })
//
//    val preExistingConfirmedProposedAccommodationType = createAndSaveProposedAccommodation(
//      caseEntity = caseEntity,
//      cprAddressId = UUID.randomUUID(),
//      accommodationSource = AccommodationSource.SAS,
//      postcode = "W3 9XE",
//      buildingNumber = "511",
//      thoroughfareName = "Test street",
//      postTown = "London",
//      country = "England",
//      startDate = null,
//      verificationStatus = EntityVerificationStatus.PASSED,
//      nextAccommodationStatus = EntityNextAccommodationStatus.YES,
//      accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue(code = AddressStatusCode.PR.name),
//      accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(code = AddressUsageCode.A07B.name)!!,
//    )
//    preExistingConfirmedProposedAccommodationType.createdAt = ZonedDateTime.now().minusSeconds(11).toInstant()
//    proposedAccommodationRepository.save(preExistingConfirmedProposedAccommodationType)
//
//    val preExistingUnconfirmedAccommodationType = accommodationTypeRepository.findByCodeAndActiveIsTrue(code = AddressUsageCode.A01A.name)!!
//    val preExistingUnconfirmedProposedAccommodationEntity = createAndSaveProposedAccommodation(
//      caseEntity = caseEntity,
//      cprAddressId = null,
//      accommodationSource = AccommodationSource.SAS,
//      postcode = "W1 8XX",
//      buildingNumber = "11",
//      thoroughfareName = "Piccadilly Circus",
//      postTown = "London",
//      country = null,
//      verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
//      nextAccommodationStatus = EntityNextAccommodationStatus.TO_BE_DECIDED,
//      startDate = LocalDate.now(),
//      accommodationStatusEntity = null,
//      accommodationTypeEntity = preExistingUnconfirmedAccommodationType,
//    )
//
//    CorePersonRecordStubs.getCorePersonRecordOKResponse(
//      crn = crn,
//      response = buildCorePersonRecord(
//        identifiers = buildIdentifiers(crns = listOf(crn), prisonNumbers = listOf("PRI1")),
//        addresses = emptyList(),
//      ),
//    )
//
//    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
//      .withDeliusUserJwt()
//      .exchangeSuccessfully()
//      .expectBody<String>()
//      .returnResult()
//      .responseBody!!
//
//    val results = proposedAccommodationRepository.findAll()
//    assertThat(results).hasSize(2)
//
//    val softDeletedRecord = results.filter { it.deleted }
//    assertThat(softDeletedRecord).hasSize(1)
//    assertThat(softDeletedRecord.first().id).isEqualTo(preExistingConfirmedProposedAccommodationType.id)
//
//    val notDeletedRecord = results.filter { !it.deleted }
//    assertThat(notDeletedRecord).hasSize(1)
//    assertThat(notDeletedRecord.first().id).isEqualTo(preExistingUnconfirmedProposedAccommodationEntity.id)
//
//    assertThat(outboxEventRepository.findAll().size).isEqualTo(0)
//  }
//
//  @Test
//  fun `should NOT delete records that are not in nDelius if they were created in the last 10 seconds - mitigates race condition`() {
//    // simulates creating "Confirmed" Proposed Accommodation right now
//    val confirmedProposedAccommodationCreatedRightNow = createAndSaveProposedAccommodation(
//      caseEntity = caseEntity,
//      cprAddressId = UUID.randomUUID(),
//      accommodationSource = AccommodationSource.SAS,
//      postcode = "W3 9XE",
//      buildingNumber = "511",
//      thoroughfareName = "Test street",
//      postTown = "London",
//      country = "England",
//      startDate = null,
//      verificationStatus = EntityVerificationStatus.PASSED,
//      nextAccommodationStatus = EntityNextAccommodationStatus.YES,
//      accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue(code = AddressStatusCode.PR.name),
//      accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(code = AddressUsageCode.A07B.name)!!,
//    )
//    confirmedProposedAccommodationCreatedRightNow.createdAt = ZonedDateTime.now().toInstant()
//    proposedAccommodationRepository.save(confirmedProposedAccommodationCreatedRightNow)
//
//    // addresses comes back empty - not yet arrived as just created
//    CorePersonRecordStubs.getCorePersonRecordOKResponse(
//      crn = crn,
//      response = buildCorePersonRecord(
//        identifiers = buildIdentifiers(crns = listOf(crn), prisonNumbers = listOf("PRI1")),
//        addresses = emptyList(),
//      ),
//    )
//    // get and sync with CPR / nDelius
//    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
//      .withDeliusUserJwt()
//      .exchangeSuccessfully()
//      .expectBody<String>()
//      .returnResult()
//      .responseBody!!
//
//    val results = proposedAccommodationRepository.findAll()
//    assertThat(results).hasSize(1)
//
//    val softDeletedRecords = results.filter { it.deleted }
//    assertThat(softDeletedRecords).hasSize(0)
//
//    val notDeletedRecords = results.filter { !it.deleted }
//    assertThat(notDeletedRecords).hasSize(1)
//    assertThat(notDeletedRecords.first().id).isEqualTo(confirmedProposedAccommodationCreatedRightNow.id)
//
//    assertThat(outboxEventRepository.findAll().size).isEqualTo(0)
//  }

  @Test
  fun `should return expected proposed accommodation timeline for Delius Origin records and show further Delius update`() {
    val crn = "X12345"
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })
    val (deliusSyncedRecord, _) = shouldInsertUnknownDeliusOriginRecordAndThenSyncFurtherUpdate(crn)
    restTestClient.get().uri("/cases/{crn}/proposed-accommodations/{id}/timeline", crn, deliusSyncedRecord.id)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedProposedAccommodationTimeResponseForDeliusOriginAudits(
            proposedAccommodationId = deliusSyncedRecord.id,
            caseId = caseEntity.id,
          ),
        )
      }
  }

  @Test
  fun `should return expected proposed accommodation timeline for Delius Origin record with further Delius update and the final SAS update also`() {
    val crn = "ABCDEFG"
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })

    // given Delius create and update
    val (deliusSyncedRecord) = shouldInsertUnknownDeliusOriginRecordAndThenSyncFurtherUpdate(crn)

    // when sas update
    val sasUpdatedBuildingNumber = "100"
    restTestClient.put().uri("/cases/$crn/proposed-accommodations/${deliusSyncedRecord.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          accommodationTypeCode = accommodationTypeRepository.findByIdOrNull(deliusSyncedRecord.accommodationTypeId!!)!!.code,
          accommodationStatusCode = accommodationStatusRepository.findByIdOrNull(deliusSyncedRecord.accommodationStatusId!!)!!.code,
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
          subBuildingName = deliusSyncedRecord.subBuildingName,
          buildingName = deliusSyncedRecord.buildingName,
          buildingNumber = sasUpdatedBuildingNumber,
          thoroughfareName = deliusSyncedRecord.throughfareName,
          dependentLocality = deliusSyncedRecord.dependentLocality,
          postTown = deliusSyncedRecord.postTown,
          county = deliusSyncedRecord.county,
          country = deliusSyncedRecord.country,
          postcode = deliusSyncedRecord.postcode!!,
          startDate = deliusSyncedRecord.startDate?.toString(),
          endDate = deliusSyncedRecord.endDate?.toString(),
          uprn = deliusSyncedRecord.uprn,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    // when get timeline
    val commitTimesAsc = getCommitTimesAsc(deliusSyncedRecord.id)
    assertThat(commitTimesAsc).hasSize(3)

    restTestClient.get().uri("/cases/{crn}/proposed-accommodations/{id}/timeline", crn, deliusSyncedRecord.id)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        // then correct timeline response
        assertThatJson(it!!).matchesExpectedJson(
          expectedProposedAccommodationTimeResponseForDeliusAndSasAudits(
            proposedAccommodationId = deliusSyncedRecord.id,
            caseId = caseEntity.id,
            sasCommitDateTime = commitTimesAsc[2].truncatedTo(ChronoUnit.SECONDS).toString(),
          ),
        )
      }
  }

  private fun createAndSaveProposedAccommodation(
    caseEntity: CaseEntity,
    cprAddressId: UUID?,
    accommodationSource: AccommodationSource,
    postcode: String,
    buildingNumber: String,
    thoroughfareName: String,
    postTown: String?,
    country: String?,
    startDate: LocalDate?,
    accommodationStatusEntity: AccommodationStatusEntity?,
    verificationStatus: EntityVerificationStatus?,
    nextAccommodationStatus: EntityNextAccommodationStatus?,
    accommodationTypeEntity: AccommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue("A07B")!!,
  ): ProposedAccommodationEntity {
    val entity = buildProposedAccommodationEntity(
      caseId = caseEntity.id,
      cprAddressId = cprAddressId,
      accommodationSource = accommodationSource,
      name = null,
      accommodationTypeEntity = accommodationTypeEntity,
      accommodationStatusEntity = accommodationStatusEntity,
      verificationStatus = verificationStatus,
      nextAccommodationStatus = nextAccommodationStatus,
      postcode = postcode,
      buildingNumber = buildingNumber,
      throughfareName = thoroughfareName,
      postTown = postTown,
      country = country,
      startDate = startDate,
    )
    return proposedAccommodationRepository.save(entity)
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
}
