package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesandoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOfficer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCaseListResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCaseResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationOasysStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.SasAndDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.DUTY_TO_REFER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_CASE
import java.util.UUID

class CaseControllerIT : IntegrationTestBase() {
  private val log = LoggerFactory.getLogger(javaClass)

  @Value("\${case-list.page-size:1}")
  private lateinit var pageSize: String

  @Autowired
  private lateinit var caseRepository: CaseRepository

  private val crns = (1..20).map { "FAKECRN$it" }
  private val nomsNumbers = (1..20).map { "PRI$it" }

  lateinit var deliusUser: UserEntity

  @BeforeEach
  fun setup() {
    databaseUtils.truncate(DUTY_TO_REFER, SAS_CASE)

    deliusUser = createTestDataSetupUserAndDeliusUser().second
    HmppsAuthStubs.stubGrantToken()

    stubInitialCorePersonRecords()
    stubInitialRoshAndTier()
  }

  @Test
  fun `does not add identifiers from CorePersonRecord`() {
    // case 1 identifiers
    val knownCrnForCase1 = "knownCrnForCase1"

    // case 2 identifiers
    val knownCrnForCase2 = "knownCrnForCase2"
    val knownPrisonNumberCase2 = "knownPrisonNumberForCase2"

    // case 3 identifiers
    val unknownCaseCrn = "unknownCRN"
    val unknownCasePrisonNumber = "unknownPrisonNumber"

    // case 4 identifiers
    val unkownCrnCase4 = "crnToAddForCase4"
    val unkownPrisonNumberCase4 = "prisonNumberToAddForCase4"

    // case 5 identifiers
    val unknownCrnCase2 = "crnToAddForCase5"
    val unknownPrisonNumberCase2 = "prisonNumberToAddForCase5"

    // save some known about cases
    val case1 = buildCaseEntity {
      withCrn(knownCrnForCase1)
    }
    val case2 = buildCaseEntity {
      withCrn(knownCrnForCase2)
      withPrisonNumber(knownPrisonNumberCase2)
    }

    caseRepository.saveAllAndFlush(listOf(case1, case2))

    val staff = buildOfficer(username = deliusUser.username)

    // build a case-list for the 3 unknown cases
    val cases = listOf(
      buildCase(crn = unkownCrnCase4, nomsNumber = unkownPrisonNumberCase4, staff = staff),
      buildCase(crn = unknownCrnCase2, nomsNumber = unknownPrisonNumberCase2, staff = staff),
      buildCase(crn = unknownCaseCrn, nomsNumber = unknownCasePrisonNumber, staff = staff),
    )

    // the case list returned should not match any persisted CRNs
    assertThat(
      caseRepository.findAllByIdentifiers(
        crns = cases.map { it.crn },
        prisonNumbers = cases.map { it.nomsNumber!! },
      ),
    ).hasSize(0)

    SasAndDeliusStubs.stubGetCaseListByUsername(
      deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
      cases = cases,
      pageSize = pageSize.toInt(),
    )

    // SAS knows about 2 cases
    assertThat(caseRepository.findAll()).hasSize(2)

    restTestClient.get().uri { it.path("/case-list").build() }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody()
      .jsonPath("$.data.length()").isEqualTo(3)

    // the 3 unknown ones should be added
    assertThat(caseRepository.findAll()).hasSize(5)

    assertCaseIdentifiers(
      crn = unkownCrnCase4,
      expectedIdentifiers = listOf(unkownCrnCase4, unkownPrisonNumberCase4),
    )

    assertCaseIdentifiers(
      crn = unknownCrnCase2,
      expectedIdentifiers = listOf(unknownCrnCase2, unknownPrisonNumberCase2),
    )

    assertCaseIdentifiers(
      crn = unknownCaseCrn,
      expectedIdentifiers = listOf(unknownCaseCrn, unknownCasePrisonNumber),
    )
  }

  private fun assertCaseIdentifiers(
    crn: String,
    expectedIdentifiers: List<String?>,
  ) {
    val case = caseRepository.findByCrn(crn)!!

    assertThat(case.latestCrn()).isEqualTo(crn)

    assertThat(case.caseIdentifiers)
      .extracting<String> { it.identifier }
      .hasSize(expectedIdentifiers.size)
      .containsExactlyInAnyOrderElementsOf(expectedIdentifiers)
  }

  @Test
  fun `should update existing, create new and return expected case list`() {
    // there are 20 crns created and stubbed for the case list.
    stubCaseList()
    // there are 10 added to the SAS database
    seedCaseEntities()
    // and 10 we will need to call CPR for. 2 of these are errors.
    stubAdditionalCorePersonRecords()

    assertThat(caseRepository.findAll().size).isEqualTo(10)

    val result = restTestClient.get().uri { it.path("/case-list").build() }
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    result.expectBody().jsonPath("$.data.length()").isEqualTo(20)

    assertThat(caseRepository.findAll().size).isEqualTo(20)

    result.expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCaseListResponse())
      }

    // verify we call case-list endpoint 20 times (once per CRN)
    sasWiremock.verify(
      20,
      getRequestedFor(WireMock.urlPathMatching("/case-list/$USERNAME_OF_LOGGED_IN_DELIUS_USER")),
    )
  }

  @Test
  fun `should filter cases based on provided search parameters`() {
    stubCaseList()
    seedCaseEntities()
    stubAdditionalCorePersonRecords()

    val failures = mutableListOf<String>()

    caseListFilters().forEach { filter ->
      val response = restTestClient.get().uri {
        it.path("/case-list")
          .queryParam(filter.queryParameter, filter.value).build()
      }
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody(object : ParameterizedTypeReference<ApiResponseDto<List<CaseDto>>>() {})
        .returnResult()
        .responseBody!!
        .data
      try {
        assertAll(
          { assertThat(response.size).isEqualTo(filter.expectedResultSize) },
          { filter.assertions.forEach { assertion -> assertion(response) } },
        )
      } catch (e: AssertionError) {
        log.error(e.stackTraceToString())
        failures += "Expected: ${filter.expectedResultSize} but was: ${response.size} \n$filter"
      }
    }
    assertThat(failures)
      .withFailMessage("Incorrect result for:\n%s", failures.joinToString("\n"))
      .isEmpty()
  }

  private fun caseListFilters() = listOf(
    CaseListFilter("searchTerm", "AAAAA", 0),
    CaseListFilter("searchTerm", "FAKECRN1", 1, listOf(containsNoLimitedCases())),
    CaseListFilter("searchTerm", "FIR", 17, listOf(containsNoLimitedCases())),
    CaseListFilter("searchTerm", "first", 17, listOf(containsNoLimitedCases())),
    CaseListFilter("searchTerm", "FiRsT M", 17, listOf(containsNoLimitedCases())),
    CaseListFilter("searchTerm", "FiRsT M Last", 0),
    CaseListFilter("searchTerm", "Zack", 1, listOf(containsNoLimitedCases())),
    CaseListFilter("riskLevel", "VERY_HIGH", 17, listOf(containsNoLimitedCases())),
    CaseListFilter("riskLevel", "MEDIUM", 1, listOf(containsNoLimitedCases())),
    CaseListFilter("riskLevel", "LOW", 0),
    CaseListFilter("riskLevel", "", 20, listOf(containsAllCaseTypes())),
    CaseListFilter("teamCode", "", 20, listOf(containsAllCaseTypes())),
    CaseListFilter("teamCode", "ABC123", 20, listOf(containsAllCaseTypes())),
    CaseListFilter("teamCode", "OTHERTEAM", 0),
  )

  private fun containsNoLimitedCases(): (List<CaseDto>) -> Unit = { response ->
    assertThat(response.map { it.userAccess })
      .doesNotContain(UserAccess.LIMITED)
  }

  private fun containsAllCaseTypes(): (List<CaseDto>) -> Unit = { response ->
    assertThat(response.map { it.userAccess })
      .contains(UserAccess.FULL, UserAccess.LIMITED)
  }

  private data class CaseListFilter(
    val queryParameter: String,
    val value: String,
    val expectedResultSize: Int,
    val assertions: List<(List<CaseDto>) -> Unit> = emptyList(),
  )

  private fun getCaseResponse(crn: String) = restTestClient.get().uri("/cases/$crn")
    .withDeliusUserJwt()
    .exchangeSuccessfully()
    .expectBody(String::class.java)

  @Test
  fun `should get case`() {
    val case = buildCase(crn = crns[0], nomsNumber = nomsNumbers[0])
    SasAndDeliusStubs.stubGetCase(deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER, crn = case.crn, response = case)

    getCaseResponse(case.crn).value {
      assertThatJson(it!!).matchesExpectedJson(expectedGetCaseResponse())
    }
  }

  @Test
  fun `returns ServerError when delius returns ServerError`() {
    val crn = crns[0]
    SasAndDeliusStubs.stubGetCaseFailure(USERNAME_OF_LOGGED_IN_DELIUS_USER, crn)
    restTestClient.get().uri("/cases/$crn")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus()
      .is5xxServerError
  }

  @Test
  fun `returns NotFound error when delius returns NotFound error`() {
    val crn = crns[0]
    SasAndDeliusStubs.stubGetCaseNotFoundFailure(USERNAME_OF_LOGGED_IN_DELIUS_USER, crn)
    restTestClient.get().uri("/cases/$crn")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `returns ServerError when CPR returns ServerError`() {
    val crn = "12345"
    val case = buildCase(crn = crn)
    SasAndDeliusStubs.stubGetCase(deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER, crn, response = case)
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)
    restTestClient.get().uri("/cases/$crn")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus()
      .is5xxServerError
  }

  @Test
  fun `returns NotFound error when CPR returns NotFound error`() {
    val crn = "12345"
    val case = buildCase(crn = crn)
    SasAndDeliusStubs.stubGetCase(deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER, crn, response = case)
    CorePersonRecordStubs.getCorePersonRecordNotFoundResponse(crn)
    restTestClient.get().uri("/cases/$crn")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus()
      .isNotFound
  }

  private fun stubInitialCorePersonRecords() {
    stubCorePersonRecord(
      crns[0],
      nomsNumbers[0],
      firstName = "First",
      lastName = "Last",
    )
    stubCorePersonRecord(
      crn = crns[1],
      prisonNumber = nomsNumbers[1],
      firstName = "Zack",
      lastName = "Smith",
    )
  }

  private fun stubInitialRoshAndTier() {
    ProbationIntegrationOasysStubs.getRoshOKResponse(
      crns[0],
      buildRoshDetails(rosh = buildRosh(riskChildrenCommunity = RiskLevel.VERY_HIGH)),
    )
    ProbationIntegrationOasysStubs.getRoshOKResponse(
      crns[1],
      buildRoshDetails(rosh = buildRosh(riskChildrenCommunity = RiskLevel.MEDIUM)),
    )

    val tier = buildTier()
    TierStubs.getTierOKResponse(crns[0], tier)
    TierStubs.getTierOKResponse(crns[1], tier)
  }

  private fun stubCaseList() {
    val staff = buildOfficer(username = deliusUser.username)
    val cases = crns.mapIndexed { i, crn ->
      buildCase(
        staff = staff,
        crn = crn,
        nomsNumber = nomsNumbers[i],
        gender = when (i) {
          3, 6 -> "Female"
          4, 7 -> "Non-Specified"
          5, 8 -> "Not Known / Not Recorded"
          else -> "Male"
        },
        name = if (i == 1) buildName("Zack", "Smith") else buildName(),
        roshLevel = if (i == 1) {
          buildRoshLevel("RMRH", "Medium")
        } else {
          buildRoshLevel()
        },
        userRestricted = when (i) {
          crns.size - 1 -> true
          else -> false
        },
        userExcluded = when (i) {
          crns.size - 2 -> true
          else -> false
        },
      )
    }

    SasAndDeliusStubs.stubGetCaseListByUsername(
      deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
      cases = cases,
      pageSize = pageSize.toInt(),
    )
  }

  private fun seedCaseEntities() {
    val entities = listOf(
      buildCaseEntity { withCrn(crns[5]) },
      buildCaseEntity(
        tierScore = "A1S",
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
      ) { withCrn(crns[6]) },
      buildCaseEntity(
        tierScore = "C1",
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
      ) { withCrn(crns[7]) },
      buildCaseEntity(
        tierScore = "B3",
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
      ) { withCrn(crns[8]) },
      buildCaseEntity(
        tierScore = "B3",
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        cas1ApplicationRequestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
        cas1ApplicationPlacementStatus = Cas1PlacementStatus.CANCELLED,
      ) { withCrn(crns[9]) },
      buildCaseEntity(
        tierScore = "B3",
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
      ) { withCrn(crns[10]) },
      buildCaseEntity(
        tierScore = "B3",
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.REJECTED,
      ) { withCrn(crns[11]) },
      buildCaseEntity(
        tierScore = "B3",
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.STARTED,
      ) { withCrn(crns[12]) },
      buildCaseEntity(
        tierScore = null,
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.WITHDRAWN,
      ) { withCrn(crns[13]) },
      buildCaseEntity(
        tierScore = "D3",
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.INAPPLICABLE,
      ) { withCrn(crns[14]) },
    )

    caseRepository.saveAll(entities)
  }

  private fun stubAdditionalCorePersonRecords() {
    (2..4).forEach { stubCorePersonRecord(crns[it], nomsNumbers[it]) }

    CorePersonRecordStubs.getCorePersonRecordNotFoundResponse(crns[15])
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crns[16])

    (17..19).forEach {
      stubCorePersonRecord(
        crn = crns[it],
        prisonNumber = nomsNumbers[it],
        additionalCrns = listOf("ADDITIONAL$it"),
      )
    }
  }

  private fun stubCorePersonRecord(
    crn: String,
    prisonNumber: String,
    firstName: String? = null,
    lastName: String? = null,
    additionalCrns: List<String> = emptyList(),
    additionalPrisonNumbers: List<String> = emptyList(),
  ) {
    val crns = (listOf(crn) + additionalCrns).distinct()
    val prisonNumbers = (listOf(prisonNumber) + additionalPrisonNumbers).distinct()
    val record = buildCorePersonRecord(
      identifiers = buildIdentifiers(crns = crns, prisonNumbers = prisonNumbers),
      firstName = firstName,
      lastName = lastName,
    )

    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn, record)
  }
}
