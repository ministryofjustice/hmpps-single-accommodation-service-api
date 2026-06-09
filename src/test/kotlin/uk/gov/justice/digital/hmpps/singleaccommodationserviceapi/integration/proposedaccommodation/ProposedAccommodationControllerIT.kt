package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.javers.core.Javers
import org.javers.repository.jql.QueryBuilder
import org.junit.jupiter.api.AfterEach
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.MutableTestClock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.ProbationCreateAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildNomisUserDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPersonName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProbationCreateAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProbationCreateAddressResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildStaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatusEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_TEST_DATA_SETUP_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_NOMIS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedGetProposedAccommodationByIdResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedGetProposedAccommodationTimelineResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedGetProposedAccommodationsEmptyResponseWithGetDeliusCaseFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedGetProposedAccommodationsResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedProposedAccommodationTimeResponseForDeliusOriginAudits
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedProposedAddressesResponseBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.expectedSasAddressUpdatedDomainEventJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.proposedAccommodationNoteRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json.proposedAddressesRequestBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.NomisUserRolesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.SasAndDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.OUTBOX_EVENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.PROPOSED_ACCOMMODATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.TestSqsDomainEventListener
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.String
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus as EntityNextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus as EntityVerificationStatus

@TestPropertySource(properties = ["scheduling.enabled=true"])
class ProposedAccommodationControllerIT : IntegrationTestBase() {
  @Autowired
  private lateinit var clock: MutableTestClock

  @Autowired
  private lateinit var accommodationTypeRepository: AccommodationTypeRepository

  @Autowired
  private lateinit var accommodationStatusRepository: AccommodationStatusRepository

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
  private val cprAddressId = UUID.randomUUID()

  private lateinit var beforeTest: Instant
  private lateinit var caseEntity: CaseEntity

  @BeforeEach
  fun setup() {
    clock.freezeAt(fixedInstant)
    beforeTest = Instant.now()
    proposedAccommodationRepository.deleteAll()
    outboxEventRepository.deleteAll()
    crn = UUID.randomUUID().toString()
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })

    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()
    createDeliusSyncUser()
    stubCurrentAccommodationIsCas1(crn)
    databaseUtils.truncate(PROPOSED_ACCOMMODATION, OUTBOX_EVENT)
  }

  @AfterEach
  fun teardown() {
    clock.reset()
  }

  private fun stubCurrentAccommodationIsCas1(crn: String) {
    val corePersonRecord = buildCorePersonRecord(
      identifiers = buildIdentifiers(crns = listOf(crn)),
      addresses = listOf(
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
              code = AddressUsageCode.A02.name,
              description = AddressUsageCode.A02.description,
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
  }

  @Test
  fun `should get proposed-accommodations by crn when there are two pre-existing SAS origin records - one 'Confirmed' and one 'Unconfirmed'`() {
    val olderEntityAccommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(code = AddressUsageCode.A07A.name)!!
    val olderEntity = createAndSaveProposedAccommodation(
      caseEntity = caseEntity,
      cprAddressId = null,
      accommodationSource = AccommodationSource.SAS,
      postcode = "RG26 5AG",
      buildingNumber = "4",
      thoroughfareName = "Dollis Green",
      postTown = "Bramley",
      country = "England",
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
      verificationStatus = EntityVerificationStatus.PASSED,
      nextAccommodationStatus = EntityNextAccommodationStatus.YES,
      startDate = LocalDate.now(),
      accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue("PR"),
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
            firstVerificationStatus = VerificationStatus.PASSED,
            firstNextAccommodationStatus = NextAccommodationStatus.YES,
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
  fun `should insert Delius origin record when does not exist in SAS database`() {
    val crn = "ABCDEFG"
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })
    shouldInsertDeliusOriginRecordWhenDoesNotExistInSasDb(
      crn,
      deliusProposedAccommodationBuildingNumber = "11",
      deliusOriginProposedAccommodationTypeCode = AddressUsageCode.A07A,
      deliusOriginProposedAccommodationStartDate = LocalDate.now().minusDays(1),
    )
  }

  @Test
  fun `should insert Delius origin record when record does not exist in SAS database and SAS case record does not exist in database either`() {
    val crn = "ABCDEFG"
    val prisonNumber = "PRI1"
    val case = buildCase(crn, nomsNumber = prisonNumber)
    SasAndDeliusStubs.stubGetCase(deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER, crn, response = case)

    shouldInsertDeliusOriginRecordWhenDoesNotExistInSasDb(
      crn,
      deliusProposedAccommodationBuildingNumber = "11",
      deliusOriginProposedAccommodationTypeCode = AddressUsageCode.A07A,
      deliusOriginProposedAccommodationStartDate = LocalDate.now().minusDays(1),
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
  fun `should NOT insert Delius origin record when record does not exist in SAS database and SAS case record does not exist either but retrieving the case from Delius fails`() {
    val crn = "ABCDEFG"
    SasAndDeliusStubs.stubGetCaseFailure(deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER, crn)
    mockCurrentPrisonAccommodationAndDeliusOriginProposedAccommodation(
      crn,
      deliusProposedAccommodationBuildingNumber = "11",
      deliusOriginProposedAccommodationTypeCode = AddressUsageCode.A07A,
      deliusOriginProposedAccommodationStartDate = LocalDate.now().minusDays(1),
    )

    val response = restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult()
      .responseBody!!

    assertThatJson(response).matchesExpectedJson(
      expectedGetProposedAccommodationsEmptyResponseWithGetDeliusCaseFailure(),
    )
    assertThat(caseRepository.findByCrn(crn)).isNull()
    assertThat(proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn)).isEmpty()
  }

  @Test
  fun `should NOT insert Delius origin record when the accommodation type is a non-Probation type`() {
    val crn = "ABCDEFG"
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })
    mockCurrentPrisonAccommodationAndDeliusOriginProposedAccommodation(
      crn,
      deliusProposedAccommodationBuildingNumber = "11",
      deliusOriginProposedAccommodationTypeCode = AddressUsageCode.HOSP,
      deliusOriginProposedAccommodationStartDate = LocalDate.now().minusDays(1),
    )
    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult()
      .responseBody!!

    val results = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn)
    assertThat(results).hasSize(0)
  }

  @Test
  fun `should NOT insert Delius origin record when record does not have an accommodation type`() {
    val crn = "ABCDEFG"
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })
    mockCurrentPrisonAccommodationAndDeliusOriginProposedAccommodation(
      crn,
      deliusProposedAccommodationBuildingNumber = "11",
      deliusOriginProposedAccommodationTypeCode = null,
      deliusOriginProposedAccommodationStartDate = LocalDate.now().minusDays(1),
    )
    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult()
      .responseBody!!

    val results = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn)
    assertThat(results).hasSize(0)
  }

  @Test
  fun `should sync Delius origin record that we already have in SAS database when there are further updates in Delius`() {
    val crn = "ABCDEFG"
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })
    shouldInsertUnknownDeliusOriginRecordAndThenSyncFurtherUpdate(crn)
  }

  @Test
  fun `should NOT sync Delius origin record once SAS updates the record as SAS becomes the owner at this stage`() {
    val crn = "ABCDEFG"
    caseEntity = caseRepository.save(buildCaseEntity { withCrn(crn) })
    val (deliusSyncedRecord, deliusOriginProposedAccommodation) = shouldInsertUnknownDeliusOriginRecordAndThenSyncFurtherUpdate(crn)
    assertThat(deliusSyncedRecord.buildingNumber).isEqualTo("15")

    val sasUpdatedBuildingNumber = "100"
    restTestClient.put().uri("/cases/$crn/proposed-accommodations/${deliusSyncedRecord.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          accommodationTypeCode = accommodationTypeRepository.findByIdOrNull(deliusSyncedRecord.accommodationTypeId)!!.code,
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

    var results = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn)
    assertThat(results).hasSize(1)

    var sasOwnedRecord = results.firstOrNull { it.accommodationSource == AccommodationSource.SAS }
    assertThat(sasOwnedRecord).isNotNull
    assertThat(sasOwnedRecord!!.buildingNumber).isEqualTo(sasUpdatedBuildingNumber)

    // mock new call to CPR which gives us a change made in nDelius for same record (different building number in address)
    val deliusUpdatedBuildingNumber = "200"
    val deliusOriginProposedAccommodationCopyWithDifferentBuildingNumber = deliusOriginProposedAccommodation.copy(
      buildingNumber = deliusUpdatedBuildingNumber,
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

    // get proposed-accommodations and ensure the latest Delius updated record has the house number from the SAS PUT and not the most recent Delius update
    val response: String = restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult()
      .responseBody!!

    results = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn)
    assertThat(results).hasSize(1)

    sasOwnedRecord = results.firstOrNull { it.accommodationSource == AccommodationSource.SAS }
    assertThat(sasOwnedRecord).isNotNull
    assertThat(sasOwnedRecord!!.buildingNumber).isEqualTo(sasUpdatedBuildingNumber)

    assertThatJson(response).matchesExpectedJson(
      expectedGetProposedAccommodationsResponse(
        firstId = deliusSyncedRecord.id,
        firstBuildingNumber = sasUpdatedBuildingNumber,
        firstCreatedBy = "nDelius user",
        firstCreatedAt = sasOwnedRecord.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        firstAccommodationTypeEntity = accommodationTypeRepository.findByIdOrNull(sasOwnedRecord.accommodationTypeId)!!,
        firstVerificationStatus = VerificationStatus.PASSED,
        firstNextAccommodationStatus = NextAccommodationStatus.YES,
        firstStartDate = deliusSyncedRecord.startDate,
        crn = crn,
      ),
    )
  }

  @Test
  fun `should NOT sync SAS origin records when someone has changed the data in Delius - as SAS is the master of that record`() {
    val startDate = LocalDate.now().minusDays(10)
    val originalHouseNumberOfSasOriginProposedAccommodation = "5"
    val updatedHouseNumberOfSasOriginProposedAccommodationInDeliusOnly = "2"
    val commonCprAddressId = UUID.randomUUID()
    val sasOriginProposedAccommodationInDelius = buildCanonicalAddress(
      cprAddressId = commonCprAddressId,
      noFixedAbode = false,
      typeVerified = false,
      postcode = "RG26 5AG",
      buildingNumber = updatedHouseNumberOfSasOriginProposedAccommodationInDeliusOnly,
      thoroughfareName = "Dollis Green",
      postTown = "Bramley",
      country = null,
      startDate = startDate,
      endDate = null,
      status = CanonicalAddressStatus(
        code = AddressStatusCode.PR.name,
        description = AddressStatusCode.PR.description,
      ),
      usage = CanonicalAddressUsage(
        usageCode = CanonicalAddressUsageCode(
          code = AddressUsageCode.A07B.name,
          description = AddressUsageCode.A07B.description,
        ),
        isActive = true,
      ),
    )
    val cprAccommodations = buildCorePersonRecord(
      identifiers = buildIdentifiers(crns = listOf(crn)),
      addresses = listOf(
        sasOriginProposedAccommodationInDelius,
      ),
    )
    CorePersonRecordStubs.getCorePersonRecordOKResponse(
      crn = crn,
      response = cprAccommodations,
    )
    val sasOriginProposedAccommodationEntity = createAndSaveProposedAccommodation(
      caseEntity = caseEntity,
      cprAddressId = commonCprAddressId,
      accommodationSource = AccommodationSource.SAS,
      postcode = "W1 8XX",
      buildingNumber = originalHouseNumberOfSasOriginProposedAccommodation,
      thoroughfareName = "Piccadilly Circus",
      postTown = "London",
      country = null,
      verificationStatus = EntityVerificationStatus.PASSED,
      nextAccommodationStatus = EntityNextAccommodationStatus.YES,
      accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(AddressUsageCode.A07B.name)!!,
      accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue(AddressStatusCode.PR.name)!!,
      startDate = startDate,
    )

    val response: String = restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult()
      .responseBody!!

    val results = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn)
    assertThat(results).hasSize(1)

    val sasOriginRecord = results.firstOrNull { it.accommodationSource == AccommodationSource.SAS }
    assertThat(sasOriginRecord).isNotNull
    assertThat(sasOriginRecord!!.buildingNumber).isEqualTo(originalHouseNumberOfSasOriginProposedAccommodation)

    assertThatJson(response).matchesExpectedJson(
      expectedGetProposedAccommodationsResponse(
        firstId = sasOriginProposedAccommodationEntity.id,
        firstBuildingNumber = originalHouseNumberOfSasOriginProposedAccommodation,
        firstCreatedBy = "Test Data Setup User",
        firstCreatedAt = sasOriginProposedAccommodationEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        firstAccommodationTypeEntity = accommodationTypeRepository.findByIdOrNull(sasOriginProposedAccommodationEntity.accommodationTypeId)!!,
        firstVerificationStatus = VerificationStatus.PASSED,
        firstNextAccommodationStatus = NextAccommodationStatus.YES,
        firstStartDate = sasOriginProposedAccommodationEntity.startDate,
        crn = crn,
      ),
    )
  }

  private fun shouldInsertDeliusOriginRecordWhenDoesNotExistInSasDb(
    crn: String,
    deliusProposedAccommodationBuildingNumber: String,
    deliusOriginProposedAccommodationTypeCode: AddressUsageCode,
    deliusOriginProposedAccommodationStartDate: LocalDate,
  ): Pair<CanonicalAddress, CanonicalAddress> {
    val (currentPrisonAccommodation, deliusOriginProposedAccommodation) = mockCurrentPrisonAccommodationAndDeliusOriginProposedAccommodation(
      crn,
      deliusProposedAccommodationBuildingNumber,
      deliusOriginProposedAccommodationTypeCode,
      deliusOriginProposedAccommodationStartDate,
    )

    val response: String = restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult()
      .responseBody!!

    val results = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn)
    assertThat(results).hasSize(1)

    val deliusSyncedRecord = results.firstOrNull { it.accommodationSource == AccommodationSource.DELIUS }
    assertThat(deliusSyncedRecord).isNotNull

    assertThatJson(response).matchesExpectedJson(
      expectedGetProposedAccommodationsResponse(
        firstId = deliusSyncedRecord!!.id,
        firstBuildingNumber = deliusProposedAccommodationBuildingNumber,
        firstCreatedBy = "nDelius user",
        firstCreatedAt = deliusSyncedRecord.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        firstAccommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(deliusOriginProposedAccommodationTypeCode.name)!!,
        firstVerificationStatus = VerificationStatus.PASSED,
        firstNextAccommodationStatus = NextAccommodationStatus.YES,
        firstStartDate = deliusOriginProposedAccommodationStartDate,
        crn = crn,
      ),
    )
    return currentPrisonAccommodation to deliusOriginProposedAccommodation
  }

  private fun mockCurrentPrisonAccommodationAndDeliusOriginProposedAccommodation(
    crn: String,
    deliusProposedAccommodationBuildingNumber: String,
    deliusOriginProposedAccommodationTypeCode: AddressUsageCode?,
    deliusOriginProposedAccommodationStartDate: LocalDate,
  ): Pair<CanonicalAddress, CanonicalAddress> {
    val deliusOriginProposedAccommodation = buildCanonicalAddress(
      cprAddressId = UUID.randomUUID(),
      noFixedAbode = false,
      typeVerified = false,
      postcode = "W1 8XX",
      buildingNumber = deliusProposedAccommodationBuildingNumber,
      thoroughfareName = "Piccadilly Circus",
      postTown = "London",
      startDate = deliusOriginProposedAccommodationStartDate,
      endDate = null,
      status = CanonicalAddressStatus(
        code = AddressStatusCode.PR1.name,
        description = AddressStatusCode.PR1.description,
      ),
      usage = CanonicalAddressUsage(
        usageCode = CanonicalAddressUsageCode(
          code = deliusOriginProposedAccommodationTypeCode?.name,
          description = deliusOriginProposedAccommodationTypeCode?.description,
        ),
        isActive = true,
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
      usage = CanonicalAddressUsage(
        usageCode = CanonicalAddressUsageCode(
          code = "HMP",
          description = "Prison",
        ),
        isActive = true,
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
    val deliusOriginProposedAccommodationStartDate = LocalDate.now().minusDays(1)
    val originalBuildingNumberInDelius = "11"
    val updatedBuildingNumberInDelius = "15"

    // steps to insert "Delius origin" record into the SAS database
    val (currentPrisonAccommodation, deliusOriginProposedAccommodation) = shouldInsertDeliusOriginRecordWhenDoesNotExistInSasDb(
      crn,
      deliusProposedAccommodationBuildingNumber = originalBuildingNumberInDelius,
      deliusOriginProposedAccommodationTypeCode,
      deliusOriginProposedAccommodationStartDate,
    )

    var results = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn)
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

    results = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn)
    assertThat(results).hasSize(1)

    deliusSyncedRecord = results.firstOrNull { it.accommodationSource == AccommodationSource.DELIUS }
    assertThat(deliusSyncedRecord).isNotNull
    assertThat(deliusSyncedRecord!!.buildingNumber).isEqualTo(updatedBuildingNumberInDelius)

    assertThatJson(response).matchesExpectedJson(
      expectedGetProposedAccommodationsResponse(
        firstId = deliusSyncedRecord.id,
        firstBuildingNumber = updatedBuildingNumberInDelius,
        firstCreatedBy = "nDelius user",
        firstCreatedAt = deliusSyncedRecord.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        firstAccommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(deliusOriginProposedAccommodationTypeCode.name)!!,
        firstVerificationStatus = VerificationStatus.PASSED,
        firstNextAccommodationStatus = NextAccommodationStatus.YES,
        firstStartDate = deliusOriginProposedAccommodationStartDate,
        crn = crn,
      ),
    )
    return Pair(deliusSyncedRecord, deliusOriginProposedAccommodation)
  }

  @Test
  fun `should get proposed-accommodation by id and crn`() {
    val entity = createAndSaveProposedAccommodation(
      caseEntity = caseEntity,
      accommodationSource = AccommodationSource.SAS,
      cprAddressId = UUID.randomUUID(),
      postcode = "W1 8XX",
      buildingNumber = "11",
      thoroughfareName = "Piccadilly Circus",
      postTown = "London",
      country = "England",
      startDate = LocalDate.now(),
      verificationStatus = EntityVerificationStatus.PASSED,
      nextAccommodationStatus = EntityNextAccommodationStatus.YES,
      accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue("PR"),
    )

    restTestClient.get().uri("/cases/{crn}/proposed-accommodations/{id}", crn, entity.id)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetProposedAccommodationByIdResponse(
            id = entity.id,
            crn = crn,
            createdAt = entity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
            startDate = LocalDate.now().toString(),
          ),
        )
      }
  }

  @Test
  fun `should return empty list when no proposed-accommodations exist for crn`() {
    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson("""{"data":[]}""")
      }
  }

  @Test
  fun `should create 'Confirmed' proposed-accommodation and POST to CPR and not publish sas-address-updated event and persist to database with cprAddressId`() {
    val addressUsageCode = AddressUsageCode.A01A
    val (expectedCprRequestBody, createProposedAccommodationResponseBody) = createConfirmedProposedAccommodation(
      addressUsageCode,
    )
    val proposedAccommodationPersistedResult = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn).first()
    val expectedAccommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(addressUsageCode.name)!!
    val expectedAccommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue("PR")!!
    assertPersistedProposedAccommodationIncludingValidCprAddressIdAndAccommodationStatus(
      proposedAccommodationPersistedResult,
      expectedAccommodationTypeEntity,
      expectedAccommodationStatusEntity,
      expectedCprRequestBody,
      createdByUserId = userIdOfLoggedInDeliusUser,
      updatedByUserId = userIdOfLoggedInDeliusUser,
    )

    assertThatJson(createProposedAccommodationResponseBody).matchesExpectedJson(
      expectedJson = expectedProposedAddressesResponseBody(
        crn = crn,
        id = proposedAccommodationPersistedResult.id,
        accommodationTypeCode = "A01A",
        accommodationTypeDescription = "Owner of the property",
        verificationStatus = VerificationStatus.PASSED.name,
        nextAccommodationStatus = NextAccommodationStatus.YES.name,
        subBuildingName = expectedCprRequestBody.subBuildingName!!,
        buildingName = expectedCprRequestBody.buildingName!!,
        buildingNumber = expectedCprRequestBody.buildingNumber!!,
        thoroughfareName = expectedCprRequestBody.thoroughfareName!!,
        dependentLocality = expectedCprRequestBody.dependentLocality!!,
        postTown = expectedCprRequestBody.postTown!!,
        county = expectedCprRequestBody.county!!,
        postcode = expectedCprRequestBody.postcode!!,
        uprn = expectedCprRequestBody.uprn!!,
        createdBy = NAME_OF_LOGGED_IN_DELIUS_USER,
        createdAt = proposedAccommodationPersistedResult.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
      ),
    )
    assertThat(outboxEventRepository.findAll()).isEmpty()
  }

  private fun createConfirmedProposedAccommodation(
    addressUsageCode: AddressUsageCode,
  ): Pair<ProbationCreateAddress, String> {
    val expectedCprRequestBody = buildProbationCreateAddress(
      noFixedAbode = false,
      subBuildingName = "test sub building name",
      buildingName = "test building name",
      buildingNumber = "4",
      thoroughfareName = "test thoroughfare",
      dependentLocality = "test dependent locality",
      postTown = "test post town",
      county = "test county",
      countryCode = null,
      postcode = "test postcode",
      uprn = "test uprn",
      startDate = fixedInstant,
      endDate = null,
      statusCode = AddressStatusCode.PR,
      usage = AddressUsage(
        usageCode = addressUsageCode,
        isActive = true,
      ),
      contacts = emptyList(),
    )
    CorePersonRecordStubs.postAddress(crn, request = expectedCprRequestBody, response = buildProbationCreateAddressResponse(crn, cprAddressId))
    val createProposedAccommodationResponseBody = restTestClient.post().uri("/cases/$crn/proposed-accommodations")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          accommodationTypeCode = addressUsageCode.name,
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
          subBuildingName = expectedCprRequestBody.subBuildingName,
          buildingName = expectedCprRequestBody.buildingName,
          buildingNumber = expectedCprRequestBody.buildingNumber,
          thoroughfareName = expectedCprRequestBody.thoroughfareName,
          dependentLocality = expectedCprRequestBody.dependentLocality,
          postTown = expectedCprRequestBody.postTown,
          county = expectedCprRequestBody.county,
          country = "England",
          postcode = expectedCprRequestBody.postcode!!,
          uprn = expectedCprRequestBody.uprn,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    return expectedCprRequestBody to createProposedAccommodationResponseBody
  }

  @Test
  fun `should create 'Unconfirmed' proposed-accommodation and NOT POST to CPR and not publish sas-address-updated event and persists to database without cprAddressId or status`() {
    val addressUsageCode = AddressUsageCode.A01A
    val createdProposedAccommodation = restTestClient.post()
      .uri("/cases/{crn}/proposed-accommodations", crn)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          startDate = "2026-01-05",
          endDate = "2026-04-25",
          subBuildingName = "test sub building name",
          buildingName = "test building name",
          buildingNumber = "4",
          thoroughfareName = "test thoroughfare",
          dependentLocality = "test dependent locality",
          postTown = "test post town",
          county = "test county",
          postcode = "test postcode",
          uprn = "test uprn",
          accommodationTypeCode = addressUsageCode.name,
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.NO.name,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    val proposedAccommodationPersistedResult = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn).first()
    val expectedAccommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(addressUsageCode.name)!!

    assertThat(proposedAccommodationPersistedResult.cprAddressId).isNull()
    assertThat(proposedAccommodationPersistedResult.accommodationStatusId).isNull()
    assertThat(proposedAccommodationPersistedResult.buildingName).isEqualTo("test building name")
    assertThat(proposedAccommodationPersistedResult.accommodationTypeId).isEqualTo(expectedAccommodationTypeEntity.id)
    assertThat(proposedAccommodationPersistedResult.accommodationSource).isEqualTo(AccommodationSource.SAS)
    assertThat(proposedAccommodationPersistedResult.typeVerified).isFalse()
    assertThat(proposedAccommodationPersistedResult.noFixedAbode).isFalse()

    assertThatJson(createdProposedAccommodation).matchesExpectedJson(
      expectedJson = expectedProposedAddressesResponseBody(
        crn = crn,
        id = proposedAccommodationPersistedResult.id,
        accommodationTypeCode = "A01A",
        accommodationTypeDescription = "Owner of the property",
        verificationStatus = VerificationStatus.PASSED.name,
        nextAccommodationStatus = NextAccommodationStatus.NO.name,
        subBuildingName = "test sub building name",
        buildingName = "test building name",
        buildingNumber = "4",
        thoroughfareName = "test thoroughfare",
        dependentLocality = "test dependent locality",
        postTown = "test post town",
        county = "test county",
        postcode = "test postcode",
        uprn = "test uprn",
        createdBy = NAME_OF_LOGGED_IN_DELIUS_USER,
        createdAt = proposedAccommodationPersistedResult.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
      ),
    )
    assertThat(outboxEventRepository.findAll()).isEmpty()
  }

  @Test
  fun `should receive 5xx Error for create proposed-accommodation when current accommodation has 5xx upstream failure`() {
    val currentAccommodation5xxWireMockedCrn = "X123"
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn = currentAccommodation5xxWireMockedCrn)
    restTestClient.post().uri("/cases/$currentAccommodation5xxWireMockedCrn/proposed-accommodations")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          accommodationTypeCode = "A01A",
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
          subBuildingName = "test sub building name",
          buildingName = "test building name",
          buildingNumber = "4",
          thoroughfareName = "test thoroughfare",
          dependentLocality = "test dependent locality",
          postTown = "test post town",
          county = "test county",
          country = "England",
          postcode = "test postcode",
          uprn = "test uprn",
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().is5xxServerError
  }

  @Test
  fun `should receive NOT_FOUND Error for create proposed-accommodation when current accommodation has NOT_FOUND upstream failure`() {
    val currentAccommodation4xxWireMockedCrn = "X123"
    CorePersonRecordStubs.getCorePersonRecordNotFoundResponse(crn = currentAccommodation4xxWireMockedCrn)
    restTestClient.post().uri("/cases/$currentAccommodation4xxWireMockedCrn/proposed-accommodations")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          accommodationTypeCode = "A01A",
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().is4xxClientError
  }

  @Test
  fun `should update 'Unconfirmed' proposed-accommodation to be 'Confirmed' and so POST to CPR and not publish sas-address-updated event and persist to database with cprAddressId and status`() {
    val existingEntity = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue("A07B")!!,
        accommodationStatusEntity = null,
        subBuildingName = "test sub building name",
        buildingName = "test building name",
        buildingNumber = "4",
        throughfareName = "test thoroughfare",
        dependentLocality = "test dependent locality",
        postTown = "test post town",
        county = "test county",
        country = "England",
        postcode = "test postcode",
        uprn = "test uprn",
        caseId = caseEntity.id,
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
      ),
    )
    val addressUsageCode = AddressUsageCode.A01A
    val expectedCprRequestBody = buildProbationCreateAddress(
      noFixedAbode = false,
      subBuildingName = "test sub building name",
      buildingName = "test building name",
      buildingNumber = "4",
      thoroughfareName = "test thoroughfare",
      dependentLocality = "test dependent locality",
      postTown = "test post town",
      county = "test county",
      countryCode = null,
      postcode = "test postcode",
      uprn = "test uprn",
      startDate = fixedInstant,
      endDate = null,
      statusCode = AddressStatusCode.PR,
      usage = AddressUsage(
        usageCode = addressUsageCode,
        isActive = true,
      ),
      contacts = emptyList(),
    )
    CorePersonRecordStubs.postAddress(crn, request = expectedCprRequestBody, response = buildProbationCreateAddressResponse(crn, cprAddressId))

    val result = restTestClient.put().uri("/cases/$crn/proposed-accommodations/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          accommodationTypeCode = addressUsageCode.name,
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
          subBuildingName = expectedCprRequestBody.subBuildingName,
          buildingName = expectedCprRequestBody.buildingName,
          buildingNumber = expectedCprRequestBody.buildingNumber,
          thoroughfareName = expectedCprRequestBody.thoroughfareName,
          dependentLocality = expectedCprRequestBody.dependentLocality,
          postTown = expectedCprRequestBody.postTown,
          county = expectedCprRequestBody.county,
          country = "England",
          postcode = expectedCprRequestBody.postcode!!,
          uprn = expectedCprRequestBody.uprn,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    val proposedAccommodationPersistedResult = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn).first()
    val expectedAccommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(addressUsageCode.name)!!
    val expectedAccommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue("PR")!!
    assertPersistedProposedAccommodationIncludingValidCprAddressIdAndAccommodationStatus(
      proposedAccommodationPersistedResult,
      expectedAccommodationTypeEntity,
      expectedAccommodationStatusEntity,
      expectedCprRequestBody,
      createdByUserId = userIdOfTestDataSetupUser,
      updatedByUserId = userIdOfLoggedInDeliusUser,
    )
    assertThatJson(result).matchesExpectedJson(
      expectedJson = expectedProposedAddressesResponseBody(
        id = existingEntity.id,
        accommodationTypeCode = "A01A",
        accommodationTypeDescription = "Owner of the property",
        verificationStatus = VerificationStatus.PASSED.name,
        nextAccommodationStatus = NextAccommodationStatus.YES.name,
        subBuildingName = expectedCprRequestBody.subBuildingName!!,
        buildingName = expectedCprRequestBody.buildingName!!,
        buildingNumber = expectedCprRequestBody.buildingNumber!!,
        thoroughfareName = expectedCprRequestBody.thoroughfareName!!,
        dependentLocality = expectedCprRequestBody.dependentLocality!!,
        postTown = expectedCprRequestBody.postTown!!,
        county = expectedCprRequestBody.county!!,
        postcode = expectedCprRequestBody.postcode!!,
        uprn = expectedCprRequestBody.uprn!!,
        createdBy = NAME_OF_TEST_DATA_SETUP_USER,
        createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        crn = crn,
      ),
    )
    assertThat(outboxEventRepository.findAll()).isEmpty()
  }

  @Test
  fun `should update 'Confirmed' proposed-accommodation and so should publish sas-address-updated event and should not POST to CPR`() {
    val existingEntity = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        cprAddressId = cprAddressId,
        accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue("A07B")!!,
        accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue("PR")!!,
        subBuildingName = "test sub building name",
        buildingName = "test building name",
        buildingNumber = "4",
        throughfareName = "test thoroughfare",
        dependentLocality = "test dependent locality",
        postTown = "test post town",
        county = "test county",
        country = "England",
        postcode = "test postcode",
        uprn = "test uprn",
        caseId = caseEntity.id,
        verificationStatus = EntityVerificationStatus.PASSED,
        nextAccommodationStatus = EntityNextAccommodationStatus.YES,
      ),
    )
    val addressUsageCode = AddressUsageCode.A01A
    val result = restTestClient.put().uri("/cases/$crn/proposed-accommodations/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          accommodationTypeCode = addressUsageCode.name,
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
          subBuildingName = "test sub building name",
          buildingName = "NEW BUILDING NAME",
          buildingNumber = "4",
          thoroughfareName = "test thoroughfare",
          dependentLocality = "test dependent locality",
          postTown = "test post town",
          county = "test county",
          country = "England",
          postcode = "test postcode",
          uprn = "test uprn",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    val proposedAccommodationPersistedResult = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn).first()
    val expectedAccommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(addressUsageCode.name)!!
    val expectedAccommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue("PR")!!

    assertThat(proposedAccommodationPersistedResult.buildingName).isEqualTo("NEW BUILDING NAME")
    assertThat(proposedAccommodationPersistedResult.postcode).isEqualTo("test postcode")
    assertThat(proposedAccommodationPersistedResult.cprAddressId).isEqualTo(cprAddressId)
    assertThat(proposedAccommodationPersistedResult.accommodationTypeId).isEqualTo(expectedAccommodationTypeEntity.id)
    assertThat(proposedAccommodationPersistedResult.accommodationStatusId).isEqualTo(expectedAccommodationStatusEntity.id)
    assertThat(proposedAccommodationPersistedResult.verificationStatus).isEqualTo(EntityVerificationStatus.PASSED)
    assertThat(proposedAccommodationPersistedResult.nextAccommodationStatus).isEqualTo(EntityNextAccommodationStatus.YES)
    assertThat(proposedAccommodationPersistedResult.accommodationSource).isEqualTo(AccommodationSource.SAS)
    assertThat(proposedAccommodationPersistedResult.typeVerified).isTrue()
    assertThat(proposedAccommodationPersistedResult.noFixedAbode).isFalse()

    assertThatJson(result).matchesExpectedJson(
      expectedJson = expectedProposedAddressesResponseBody(
        id = existingEntity.id,
        accommodationTypeCode = "A01A",
        accommodationTypeDescription = "Owner of the property",
        verificationStatus = VerificationStatus.PASSED.name,
        nextAccommodationStatus = NextAccommodationStatus.YES.name,
        subBuildingName = "test sub building name",
        buildingName = "NEW BUILDING NAME",
        buildingNumber = "4",
        thoroughfareName = "test thoroughfare",
        dependentLocality = "test dependent locality",
        postTown = "test post town",
        county = "test county",
        postcode = "test postcode",
        uprn = "test uprn",
        createdBy = NAME_OF_TEST_DATA_SETUP_USER,
        createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        crn = crn,
      ),
    )

    val detailUrl = "http://api-host/proposed-accommodations/${proposedAccommodationPersistedResult.id}"
    testSqsDomainEventListener.assertMessageReceived(
      typeName = SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_UPDATED.typeName,
      eventDescription = SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_UPDATED.typeDescription,
      detailUrl = detailUrl,
      externalId = null,
    )

    assertThatOutboxIsAsExpected(
      proposedAccommodationId = proposedAccommodationPersistedResult.id,
      eventType = SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_UPDATED,
    )
  }

  @Test
  fun `should downgrade 'Confirmed' proposed-accommodation to 'Unconfirmed' and so should publish sas-address-deleted event and should not POST to CPR`() {
    val confirmedStatus = EntityNextAccommodationStatus.YES
    val unconfirmedStatus = NextAccommodationStatus.NO
    val existingEntity = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        cprAddressId = cprAddressId,
        accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue("A07B")!!,
        accommodationStatusEntity = accommodationStatusRepository.findByCodeAndActiveIsTrue("PR")!!,
        subBuildingName = "test sub building name",
        buildingName = "test building name",
        buildingNumber = "4",
        throughfareName = "test thoroughfare",
        dependentLocality = "test dependent locality",
        postTown = "test post town",
        county = "test county",
        country = "England",
        postcode = "test postcode",
        uprn = "test uprn",
        caseId = caseEntity.id,
        verificationStatus = EntityVerificationStatus.PASSED,
        nextAccommodationStatus = confirmedStatus,
      ),
    )
    val addressUsageCode = AddressUsageCode.A01A
    val result = restTestClient.put().uri("/cases/$crn/proposed-accommodations/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          nextAccommodationStatus = unconfirmedStatus.name,
          verificationStatus = VerificationStatus.PASSED.name,
          accommodationTypeCode = addressUsageCode.name,
          subBuildingName = "test sub building name",
          buildingName = "test building name",
          buildingNumber = "4",
          thoroughfareName = "test thoroughfare",
          dependentLocality = "test dependent locality",
          postTown = "test post town",
          county = "test county",
          country = "England",
          postcode = "test postcode",
          uprn = "test uprn",
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    val proposedAccommodationPersistedResult = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn).first()

    assertThat(proposedAccommodationPersistedResult.cprAddressId).isEqualTo(null)
    assertThat(proposedAccommodationPersistedResult.accommodationStatusId).isEqualTo(null)
    assertThat(proposedAccommodationPersistedResult.nextAccommodationStatus).isEqualTo(EntityNextAccommodationStatus.NO)
    assertThat(proposedAccommodationPersistedResult.verificationStatus).isEqualTo(EntityVerificationStatus.PASSED)
    assertThat(proposedAccommodationPersistedResult.accommodationSource).isEqualTo(AccommodationSource.SAS)
    assertThat(proposedAccommodationPersistedResult.typeVerified).isFalse()
    assertThat(proposedAccommodationPersistedResult.noFixedAbode).isFalse()

    assertThatJson(result).matchesExpectedJson(
      expectedJson = expectedProposedAddressesResponseBody(
        id = existingEntity.id,
        accommodationTypeCode = "A01A",
        accommodationTypeDescription = "Owner of the property",
        verificationStatus = VerificationStatus.PASSED.name,
        nextAccommodationStatus = NextAccommodationStatus.NO.name,
        subBuildingName = "test sub building name",
        buildingName = "test building name",
        buildingNumber = "4",
        thoroughfareName = "test thoroughfare",
        dependentLocality = "test dependent locality",
        postTown = "test post town",
        county = "test county",
        postcode = "test postcode",
        uprn = "test uprn",
        createdBy = NAME_OF_TEST_DATA_SETUP_USER,
        createdAt = existingEntity.createdAt!!.truncatedTo(ChronoUnit.SECONDS).toString(),
        crn = crn,
      ),
    )

    testSqsDomainEventListener.assertMessageReceived(
      typeName = SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_DELETED.typeName,
      eventDescription = SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_DELETED.typeDescription,
      detailUrl = null,
      externalId = existingEntity.id,
    )

    assertThatOutboxIsAsExpected(
      proposedAccommodationId = proposedAccommodationPersistedResult.id,
      eventType = SingleAccommodationServiceDomainEventType.SAS_ACCOMMODATION_DELETED,
    )
  }

  @Test
  fun `should receive 5xx Error for update proposed-accommodation when current accommodation has upstream failures`() {
    val noCurrentAccommodationWireMockedCrn = "X123"
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn = noCurrentAccommodationWireMockedCrn)
    restTestClient.put().uri("/cases/$noCurrentAccommodationWireMockedCrn/proposed-accommodations/${UUID.randomUUID()}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          accommodationTypeCode = "A01A",
          verificationStatus = EntityVerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().is5xxServerError
  }

  @Test
  fun `should update proposed-accommodation and not publish domain event when nextAccommodationStatus is NO`() {
    val existingEntity = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue("A07B")!!,
        accommodationStatusEntity = null,
        caseId = caseEntity.id,
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
      ),
    )

    restTestClient.put().uri("/cases/$crn/proposed-accommodations/${existingEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          accommodationTypeCode = "A01A",
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
          accommodationTypeCode = "A01A",
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
    proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue("A07B")!!,
        accommodationStatusEntity = null,
        caseId = caseEntity.id,
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
      ),
    )
    val otherCase = caseRepository.save(buildCaseEntity { withCrn(UUID.randomUUID().toString()) })
    val otherEntity = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue("A07B")!!,
        accommodationStatusEntity = null,
        caseId = otherCase.id,
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
      ),
    )

    restTestClient.put().uri("/cases/$crn/proposed-accommodations/${otherEntity.id}")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          accommodationTypeCode = "A07B",
          verificationStatus = EntityVerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.YES.name,
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return proposed accommodation timeline when single 'Unconfirmed' proposed accommodation created`() {
    val accommodationTypeCode = "A01A"
    val createdProposedAccommodation = restTestClient.post()
      .uri("/cases/{crn}/proposed-accommodations", crn)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          startDate = "2026-01-05",
          endDate = "2026-04-25",
          subBuildingName = null,
          accommodationTypeCode = accommodationTypeCode,
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.NO.name,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    val createdProposedAccommodationId = ObjectMapper()
      .readTree(createdProposedAccommodation)
      .get("id").asText()

    val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdProposedAccommodationId))
    assertThat(commitTimesAsc).hasSize(1)

    val accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(accommodationTypeCode)
    restTestClient.get().uri("/cases/{crn}/proposed-accommodations/{id}/timeline", crn, createdProposedAccommodationId)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetProposedAccommodationTimelineResponse(
            proposedAccommodationId = UUID.fromString(createdProposedAccommodationId),
            accommodationDescription = accommodationTypeEntity!!.name,
            caseId = caseEntity.id,
            createCommitTime = commitTimesAsc.first()
              .truncatedTo(ChronoUnit.SECONDS).toString(),
          ),
        )
      }
  }

  @Test
  fun `should return proposed accommodation timeline when 'Confirmed' proposed accommodation is created, then a note is added, and then the proposed accommodation is updated a couple of times`() {
    val firstAccommodationType = accommodationTypeRepository.findByCodeAndActiveIsTrue("A07A")!!
    val secondAccommodationType = accommodationTypeRepository.findByCodeAndActiveIsTrue("A01A")!!
    val (expectedCprRequestBody, createProposedAccommodationResponseBody) = createConfirmedProposedAccommodation(
      addressUsageCode = AddressUsageCode.valueOf(firstAccommodationType.code),
    )
    val createdProposedAccommodationId = ObjectMapper()
      .readTree(createProposedAccommodationResponseBody)
      .get("id").asText()

    restTestClient.post()
      .uri("/cases/{crn}/proposed-accommodations/{id}/notes", crn, createdProposedAccommodationId)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAccommodationNoteRequestBody(
          note = "Test note",
        ),
      )
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

    val usernameOfNewDeliusUser = "newDeliusUser@justice.gov.uk"
    val newDeliusUserStaffDetail = buildStaffDetail(
      username = usernameOfNewDeliusUser,
      email = usernameOfNewDeliusUser,
      name = buildPersonName(
        forename = "New",
        surname = "Delius User",
      ),
    )
    ProbationIntegrationDeliusStubs.stubGetStaffByUsername(
      deliusUsername = usernameOfNewDeliusUser,
      response = newDeliusUserStaffDetail,
    )
    restTestClient.put().uri("/cases/{crn}/proposed-accommodations/{id}", crn, createdProposedAccommodationId)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          startDate = null,
          endDate = null,
          subBuildingName = "another sub building name",
          buildingName = expectedCprRequestBody.buildingName!!,
          buildingNumber = expectedCprRequestBody.buildingNumber!!,
          thoroughfareName = expectedCprRequestBody.thoroughfareName!!,
          dependentLocality = expectedCprRequestBody.dependentLocality!!,
          postTown = expectedCprRequestBody.postTown!!,
          county = expectedCprRequestBody.county!!,
          postcode = expectedCprRequestBody.postcode!!,
          uprn = expectedCprRequestBody.uprn!!,
          accommodationTypeCode = secondAccommodationType.code,
          verificationStatus = VerificationStatus.FAILED.name,
          nextAccommodationStatus = NextAccommodationStatus.NO.name,
        ),
      )
      .withDeliusUserJwt(username = usernameOfNewDeliusUser)
      .exchangeSuccessfully()

    restTestClient.put().uri("/cases/{crn}/proposed-accommodations/{id}", crn, createdProposedAccommodationId)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAddressesRequestBody(
          startDate = "2026-01-20",
          endDate = "2026-08-01",
          subBuildingName = null,
          buildingName = expectedCprRequestBody.buildingName!!,
          buildingNumber = expectedCprRequestBody.buildingNumber!!,
          thoroughfareName = expectedCprRequestBody.thoroughfareName!!,
          dependentLocality = expectedCprRequestBody.dependentLocality!!,
          postTown = expectedCprRequestBody.postTown!!,
          county = expectedCprRequestBody.county!!,
          postcode = "correct postcode",
          accommodationTypeCode = secondAccommodationType.code,
          verificationStatus = VerificationStatus.PASSED.name,
          nextAccommodationStatus = NextAccommodationStatus.NO.name,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    val commitTimesAsc = getCommitTimesAsc(UUID.fromString(createdProposedAccommodationId))
    assertThat(commitTimesAsc).hasSize(3)
    val createNoteCommitTime = proposedAccommodationRepository.findAllWithNotesByCrnOrderByCreatedAtDesc(crn).first()
      .notes.first().createdAt

    restTestClient.get().uri("/cases/{crn}/proposed-accommodations/{id}/timeline", crn, createdProposedAccommodationId)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetProposedAccommodationTimelineResponse(
            proposedAccommodationId = UUID.fromString(createdProposedAccommodationId),
            caseId = caseEntity.id,
            buildingName = expectedCprRequestBody.buildingName!!,
            buildingNumber = expectedCprRequestBody.buildingNumber!!,
            thoroughfareName = expectedCprRequestBody.thoroughfareName!!,
            dependentLocality = expectedCprRequestBody.dependentLocality!!,
            postTown = expectedCprRequestBody.postTown!!,
            county = expectedCprRequestBody.county!!,
            postcode = expectedCprRequestBody.postcode!!,
            uprn = expectedCprRequestBody.uprn!!,
            initialAccommodationTypeDescription = firstAccommodationType.name,
            updatedAccommodationTypeDescription = secondAccommodationType.name,
            createCommitTime = commitTimesAsc.first()
              .truncatedTo(ChronoUnit.SECONDS).toString(),
            createNoteCommitTime = createNoteCommitTime!!
              .truncatedTo(ChronoUnit.SECONDS).toString(),
            update1Author = "${newDeliusUserStaffDetail.name.forename} ${newDeliusUserStaffDetail.name.surname}",
            update1CommitTime = commitTimesAsc[1]
              .truncatedTo(ChronoUnit.SECONDS).toString(),
            update2CommitTime = commitTimesAsc[2]
              .truncatedTo(ChronoUnit.SECONDS).toString(),
          ),
        )
      }
  }

  @Test
  fun `should return proposed accommodation timeline for Delius Origin records and show further updates`() {
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
            startDate = deliusSyncedRecord.startDate.toString(),
          ),
        )
      }
  }

  @Test
  fun `should create a note for proposed-accommodation`() {
    val existingEntity = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue("A07B")!!,
        accommodationStatusEntity = null,
        name = "Old Name",
        caseId = caseEntity.id,
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
      ),
    )
    val note1Value = "Test note 1"
    val note2Value = "Test note 2"
    restTestClient.post().uri("/cases/$crn/proposed-accommodations/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAccommodationNoteRequestBody(
          note = note1Value,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    var proposedAccommodationPersistedResult = proposedAccommodationRepository.findAllWithNotesByCrnOrderByCreatedAtDesc(crn).first()
    assertThat(proposedAccommodationPersistedResult.notes.first().note).isEqualTo(note1Value)

    restTestClient.post().uri("/cases/$crn/proposed-accommodations/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAccommodationNoteRequestBody(
          note = note2Value,
        ),
      )
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    proposedAccommodationPersistedResult = proposedAccommodationRepository.findAllWithNotesByCrnOrderByCreatedAtDesc(crn).first()
    val sortedNotes: List<ProposedAccommodationNoteEntity> = proposedAccommodationPersistedResult.notes.sortedByDescending { it.createdAt }
    assertThat(sortedNotes.first().note).isEqualTo(note2Value)
    assertThat(sortedNotes[1].note).isEqualTo(note1Value)
  }

  @Test
  fun `should not create a note for proposed-accommodation when accommodation not found`() {
    restTestClient.post().uri("/cases/$crn/proposed-accommodations/${UUID.randomUUID()}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAccommodationNoteRequestBody(
          note = "Test note",
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should not create a note when crn not found`() {
    val unknownCrn = "54321"
    stubCurrentAccommodationIsCas1(crn = unknownCrn)
    val existingEntity = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue("A07B")!!,
        accommodationStatusEntity = null,
        name = "Old Name",
        caseId = caseEntity.id,
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
      ),
    )
    restTestClient.post().uri("/cases/$unknownCrn/proposed-accommodations/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAccommodationNoteRequestBody(
          note = "Test note",
        ),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should fail with Bad Request for empty note`() {
    val existingEntity = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue("A07B")!!,
        accommodationStatusEntity = null,
        name = "Old Name",
        caseId = caseEntity.id,
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
      ),
    )
    val note = ""
    restTestClient.post().uri("/cases/$crn/proposed-accommodations/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAccommodationNoteRequestBody(note),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should fail with Bad Request for note exceeding 4000 characters`() {
    val existingEntity = proposedAccommodationRepository.save(
      buildProposedAccommodationEntity(
        accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue("A07B")!!,
        accommodationStatusEntity = null,
        name = "Old Name",
        caseId = caseEntity.id,
        verificationStatus = EntityVerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = EntityNextAccommodationStatus.NO,
      ),
    )
    val note = "a".repeat(4001)
    restTestClient.post().uri("/cases/$crn/proposed-accommodations/${existingEntity.id}/notes")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        proposedAccommodationNoteRequestBody(note),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
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

  private fun assertPersistedProposedAccommodationIncludingValidCprAddressIdAndAccommodationStatus(
    proposedAccommodationEntity: ProposedAccommodationEntity,
    accommodationTypeEntity: AccommodationTypeEntity,
    accommodationStatusEntity: AccommodationStatusEntity,
    probationCreateAddress: ProbationCreateAddress,
    createdByUserId: UUID,
    updatedByUserId: UUID,
  ) {
    assertThat(proposedAccommodationEntity.cprAddressId).isEqualTo(cprAddressId)
    assertThat(proposedAccommodationEntity.accommodationSource).isEqualTo(AccommodationSource.SAS)
    assertThat(proposedAccommodationEntity.name).isNull()
    assertThat(proposedAccommodationEntity.accommodationTypeId).isEqualTo(accommodationTypeEntity.id)
    assertThat(proposedAccommodationEntity.accommodationStatusId).isEqualTo(accommodationStatusEntity.id)
    assertThat(proposedAccommodationEntity.verificationStatus).isEqualTo(EntityVerificationStatus.PASSED)
    assertThat(proposedAccommodationEntity.nextAccommodationStatus).isEqualTo(EntityNextAccommodationStatus.YES)
    assertThat(proposedAccommodationEntity.typeVerified).isTrue()
    assertThat(proposedAccommodationEntity.noFixedAbode).isFalse()
    assertThat(proposedAccommodationEntity.postcode).isEqualTo(probationCreateAddress.postcode)
    assertThat(proposedAccommodationEntity.subBuildingName).isEqualTo(probationCreateAddress.subBuildingName)
    assertThat(proposedAccommodationEntity.buildingName).isEqualTo(probationCreateAddress.buildingName)
    assertThat(proposedAccommodationEntity.buildingNumber).isEqualTo(probationCreateAddress.buildingNumber)
    assertThat(proposedAccommodationEntity.throughfareName).isEqualTo(probationCreateAddress.thoroughfareName)
    assertThat(proposedAccommodationEntity.dependentLocality).isEqualTo(probationCreateAddress.dependentLocality)
    assertThat(proposedAccommodationEntity.postTown).isEqualTo(probationCreateAddress.postTown)
    assertThat(proposedAccommodationEntity.county).isEqualTo(probationCreateAddress.county)
    assertThat(proposedAccommodationEntity.uprn).isEqualTo(probationCreateAddress.uprn)
    assertThat(proposedAccommodationEntity.startDate).isEqualTo(LocalDate.of(2026, 1, 5))
    assertThat(proposedAccommodationEntity.endDate).isEqualTo(LocalDate.of(2026, 4, 25))
    assertThat(proposedAccommodationEntity.createdByUserId).isEqualTo(createdByUserId)
    assertThat(proposedAccommodationEntity.createdAt).isBetween(
      beforeTest.minusSeconds(1),
      Instant.now().plusSeconds(1),
    )
    assertThat(proposedAccommodationEntity.lastUpdatedByUserId).isEqualTo(updatedByUserId)
  }

  private fun assertThatOutboxIsAsExpected(
    proposedAccommodationId: UUID,
    eventType: SingleAccommodationServiceDomainEventType,
  ) {
    val outboxRecord = outboxEventRepository.findAll().first()
    assertThat(outboxRecord.aggregateId).isEqualTo(proposedAccommodationId)
    assertThat(outboxRecord.aggregateType).isEqualTo("ProposedAccommodation")
    assertThat(outboxRecord.domainEventType).isEqualTo(eventType.name)
    assertThatJson(outboxRecord.payload).matchesExpectedJson(expectedSasAddressUpdatedDomainEventJson(proposedAccommodationId, eventType))
    assertThat(outboxRecord.processedStatus).isEqualTo(ProcessedStatus.PROCESSED)
  }
}
