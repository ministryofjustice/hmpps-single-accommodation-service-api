package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import io.mockk.every
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.core.ParameterizedTypeReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseTeam
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOfficer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCaseListResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCaseResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCaseResponseSearch
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.SasAndDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.DUTY_TO_REFER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_CASE
import java.time.LocalDate

@Import(CaseControllerIT.CaseListV2FeatureFlagTestConfig::class)
class CaseControllerIT : IntegrationTestBase() {
  private val log = LoggerFactory.getLogger(javaClass)

  @TestConfiguration
  class CaseListV2FeatureFlagTestConfig {
    @Bean
    @Primary
    fun caseQueryService(
      caseOrchestrationService: CaseOrchestrationService,
      userService: UserService,
      caseRepository: CaseRepository,
    ): CaseQueryService = spyk(
      CaseQueryService(
        caseOrchestrationService = caseOrchestrationService,
        userService = userService,
        caseRepository = caseRepository,
        caseListV2Enabled = false,
      ),
    )
  }

  @Value("\${case-list.page-size:1}")
  private lateinit var pageSize: String

  @Autowired
  private lateinit var caseRepository: CaseRepository

  @Autowired
  private lateinit var caseQueryService: CaseQueryService

  private val crns = (1..20).map { "FAKECRN$it" }
  private val nomsNumbers = (1..20).map { "PRI$it" }

  lateinit var deliusUser: UserEntity

  @BeforeEach
  fun setup() {
    databaseUtils.truncate(DUTY_TO_REFER, SAS_CASE)

    deliusUser = createTestDataSetupUserAndDeliusUser().second
    HmppsAuthStubs.stubGrantToken()

    stubInitialCorePersonRecords()
    ApprovedPremisesStubs.getCas1UrlTemplatesOKResponse()
    ApprovedPremisesStubs.getCas3UrlTemplatesOKResponse()
    val tier = buildTier()
    TierStubs.getTierOKResponse(crns[0], tier)
    TierStubs.getTierOKResponse(crns[1], tier)
  }

  @AfterEach
  fun resetCaseListV2Flag() {
    every { caseQueryService.caseListV2Enabled } returns false
  }

  private fun setCaseListV2Enabled(v2Enabled: Boolean) {
    every { caseQueryService.caseListV2Enabled } returns v2Enabled
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `does not add identifiers from CorePersonRecord`(v2Enabled: Boolean) {
    setCaseListV2Enabled(v2Enabled)

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

    SasAndDeliusStubs.stubCaseList(
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

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `should update existing, create new and return expected case list`(v2Enabled: Boolean) {
    setCaseListV2Enabled(v2Enabled)

    // there are 20 crns created and stubbed for the case list.
    val cases = stubCaseList()

    val preSeededCount = if (v2Enabled) {
      // v2 renders forename/surname/dateOfBirth from the DB CaseEntity, so pre-seed all
      seedAllCaseEntitiesForV2(cases)
      cases.size
    } else {
      // there are 10 added to the SAS database
      seedCaseEntities()
      tierScoresByCrn.size
    }
    // and 10 we will need to call CPR for. 2 of these are errors.
    stubAdditionalCorePersonRecords()

    assertThat(caseRepository.findAll().size).isEqualTo(preSeededCount)

    val result = restTestClient.get().uri { it.path("/case-list").build() }
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    result.expectBody().jsonPath("$.data.length()").isEqualTo(20)

    assertThat(caseRepository.findAll().size).isEqualTo(20)

    // v2 always sets middleNames to null, since CaseEntity has no middleNames column.
    // v2 also always returns an accommodationSummaries object (rather than null) once a CaseEntity exists
    val expectedJson = expectedGetCaseListResponse().let {
      if (v2Enabled) {
        it.replace("\"middleNames\":\"Middle\"", "\"middleNames\":null")
          .replace(
            "\"limitedAccess\":false,\n         \"accommodationSummaries\":null",
            "\"limitedAccess\":false,\n         \"accommodationSummaries\":{\"caseAccommodationStatus\":null,\"caseAccommodationStatusDate\":null,\"currentAccommodation\":null,\"nextAccommodation\":null}",
          )
      } else {
        it
      }
    }

    result.expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedJson)
      }

    // verify we call case-list endpoint 20 times (once per CRN)
    sasWiremock.verify(
      20,
      getRequestedFor(WireMock.urlPathMatching("/case-list/$USERNAME_OF_LOGGED_IN_DELIUS_USER")),
    )
  }

  @Test
  fun `should source forename, surname, dateOfBirth, tierScore and accommodationSummaries from CaseEntity when caseListV2Enabled is true`() {
    setCaseListV2Enabled(true)

    val crn = "crnWithDivergentDbAndDeliusData"
    val staff = buildOfficer(username = deliusUser.username)

    // the Delius/SAS stub returns one set of name/dateOfBirth values...
    val deliusName = buildName("DeliusForename", "DeliusSurname")
    val case = buildCase(
      crn = crn,
      staff = staff,
      name = deliusName,
      dateOfBirth = LocalDate.of(1975, 6, 15),
    )
    SasAndDeliusStubs.stubCaseList(
      deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
      cases = listOf(case),
      pageSize = pageSize.toInt(),
    )

    // different data in DB to verify that the API returns the DB data when v2 is enabled
    val preSeededCaseEntity = buildCaseEntity(
      firstName = "DbForename",
      lastName = "DbSurname",
      dateOfBirth = LocalDate.of(1990, 1, 1),
      tierScore = "D2",
    ) {
      withCrn(crn)
      currentAccommodation = buildAccommodationSummaryDto(
        crn = crn,
        status = buildAccommodationStatusDto(code = "C", description = "Custody"),
        type = buildAccommodationTypeDto(code = "HMP", description = "Prison"),
      )
      nextAccommodation = buildAccommodationSummaryDto(
        crn = crn,
        status = buildAccommodationStatusDto(code = "PR", description = "Proposed"),
        type = buildAccommodationTypeDto(code = "A07A", description = "Friends/Family (transient)"),
      )
      accommodationStatus = CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE
    }
    caseRepository.saveAndFlush(preSeededCaseEntity)

    restTestClient.get().uri { it.path("/case-list").build() }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody()
      .jsonPath("$.data[0].forename").isEqualTo(preSeededCaseEntity.firstName)
      .jsonPath("$.data[0].surname").isEqualTo(preSeededCaseEntity.lastName)
      .jsonPath("$.data[0].dateOfBirth").isEqualTo("1990-01-01")
      .jsonPath("$.data[0].tierScore").isEqualTo(preSeededCaseEntity.tierScore)
      .jsonPath("$.data[0].accommodationSummaries.caseAccommodationStatus").isEqualTo("RISK_OF_NO_FIXED_ABODE")
      .jsonPath("$.data[0].accommodationSummaries.currentAccommodation.status.code").isEqualTo("C")
      .jsonPath("$.data[0].accommodationSummaries.currentAccommodation.status.description").isEqualTo("Custody")
      .jsonPath("$.data[0].accommodationSummaries.currentAccommodation.type.code").isEqualTo("HMP")
      .jsonPath("$.data[0].accommodationSummaries.nextAccommodation.status.code").isEqualTo("PR")
      .jsonPath("$.data[0].accommodationSummaries.nextAccommodation.status.description").isEqualTo("Proposed")
      .jsonPath("$.data[0].accommodationSummaries.nextAccommodation.type.code").isEqualTo("A07A")
      .jsonPath("$.data[0].accommodationSummaries.nextAccommodation.type.description").isEqualTo("Friends/Family (transient)")
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `should only save cases that match the filtered response`(v2Enabled: Boolean) {
    setCaseListV2Enabled(v2Enabled)

    val team = buildCaseTeam("TestTeam")
    val staff = buildOfficer(username = deliusUser.username)
    val assignedCase = buildCase(crn = "crn1", staff = staff, team = team)

    val otherStaff = buildOfficer(username = "otherStaff", code = "otherStaff")
    val teamCase = buildCase(crn = "crn2", nomsNumber = "noms2", staff = otherStaff, team = team)

    SasAndDeliusStubs.stubCaseList(
      deliusUsername = deliusUser.username,
      cases = listOf(assignedCase, teamCase),
      pageSize = pageSize.toInt(),
    )

    assertThat(caseRepository.findAll()).hasSize(0)

    // request with no parameters defaults to only the cases assigned to the user, so should only return `assignedCase`
    restTestClient.get().uri { it.path("/case-list").build() }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody()
      .jsonPath("$.data.length()").isEqualTo(1)
      .jsonPath("$.data[0].assignedTo.username").isEqualTo("DELIUS_USER")

    assertThat(caseRepository.findAll()).hasSize(1)

    SasAndDeliusStubs.stubCaseList(
      deliusUsername = deliusUser.username,
      teamCode = team.code,
      cases = listOf(teamCase),
      pageSize = pageSize.toInt(),
    )

    restTestClient.get().uri { it.path("/case-list").queryParam("teamCode", team.code).build() }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody()
      .jsonPath("$.data.length()").isEqualTo(1)
      .jsonPath("$.data[0].assignedTo.username").isEqualTo("otherStaff")

    assertThat(caseRepository.findAll()).hasSize(2)
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `should filter cases based on provided search parameters`(v2Enabled: Boolean) {
    setCaseListV2Enabled(v2Enabled)

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

  @Test
  fun `should update existing, create new and return expected case`() {
    val case = buildCase(crn = crns[0], nomsNumber = nomsNumbers[0])
    SasAndDeliusStubs.stubGetCase(deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER, crn = case.crn, response = case)
    seedCaseEntities()

    assertThat(caseRepository.findAll().size).isEqualTo(10)
    val result = restTestClient.get().uri { it.path("/search/${crns[0]}").build() }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
    result.expectBody().jsonPath("$.data.length()").isEqualTo(14)

    assertThat(caseRepository.findAll().size).isEqualTo(11)

    result.expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCaseResponseSearch())
      }

    sasWiremock.verify(
      1,
      getRequestedFor(WireMock.urlPathMatching("/case/$USERNAME_OF_LOGGED_IN_DELIUS_USER/${crns[0]}")),
    )
  }

  private fun caseListFilters() = listOf(
    CaseListFilter("searchTerm", "AAAAA", 0),
    CaseListFilter("searchTerm", "FAKECRN1", 1, listOf(containsNoLimitedCases())),
    CaseListFilter("searchTerm", "FIR", 17, listOf(containsNoLimitedCases())),
    CaseListFilter("searchTerm", "first", 17, listOf(containsNoLimitedCases())),
    CaseListFilter("searchTerm", "FiRsT M", 17, listOf(containsNoLimitedCases())),
    CaseListFilter("searchTerm", "FiRsT M Last", 17, listOf(containsNoLimitedCases())),
    CaseListFilter("searchTerm", "fOrst Last", 0),
    CaseListFilter("searchTerm", "last first", 17, listOf(containsNoLimitedCases())),
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

  private fun stubCaseList(): List<Case> {
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
        name = if (i == 1) buildName("Zack", "Aardvark") else buildName(),
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

    SasAndDeliusStubs.stubCaseList(
      deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
      cases = cases,
      pageSize = pageSize.toInt(),
    )

    return cases
  }

  // tierScore values for the 10 crns pre-seeded in the SAS database ahead of the case-list
  // request; all other crns default to a null tierScore (created fresh with no tier data).
  private val tierScoresByCrn: Map<String, String?> = mapOf(
    crns[5] to "A1",
    crns[6] to "A1S",
    crns[7] to "C1",
    crns[8] to "B3",
    crns[9] to "B3",
    crns[10] to "B3",
    crns[11] to "B3",
    crns[12] to "B3",
    crns[13] to null,
    crns[14] to "D3",
  )

  private fun seedCaseEntities() {
    val entities = tierScoresByCrn.map { (crn, tierScore) ->
      buildCaseEntity(tierScore = tierScore) { withCrn(crn) }
    }

    caseRepository.saveAll(entities)
  }

  // Under caseListV2Enabled, forename/surname/dateOfBirth are rendered from the CaseEntity in
  // the DB rather than the Delius/SAS stub, so pre-seed every crn with matching data to avoid
  // depending on the async CaseRefreshWorker to backfill newly created cases.
  private fun seedAllCaseEntitiesForV2(cases: List<Case>) {
    val entities = cases.map { case ->
      buildCaseEntity(
        tierScore = tierScoresByCrn[case.crn],
        firstName = case.name.forename,
        lastName = case.name.surname,
        dateOfBirth = case.dateOfBirth,
      ) { withCrn(case.crn) }
    }

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
