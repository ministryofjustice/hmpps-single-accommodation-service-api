package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshCodeDescription
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCaseListResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCaseV2Response
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCasesResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCasesV2Response
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCasesWithFilterResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationOasysStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationSasDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import java.util.UUID

class CaseControllerIT : IntegrationTestBase() {

  @Autowired
  private lateinit var caseRepository: CaseRepository

  private val crns = (1..20).map { "FAKECRN$it" }
  private val nomsNumbers = (1..20).map { "PRI$it" }

  @BeforeEach
  fun setup() {
    createTestDataSetupUserAndDeliusUser()
    HmppsAuthStubs.stubGrantToken()

    stubInitialCaseSummaries()
    stubInitialCorePersonRecords()
    stubInitialRoshAndTier()
  }

  @AfterEach
  fun teardown() {
    caseRepository.deleteAll()
  }

  @Test
  fun `should get case list`() {
    stubCaseList()
    seedCaseEntities()
    stubAdditionalCorePersonRecords()
    stubAdditionalTierResponses()
    stubSuitableApplications()

    restTestClient.get().uri { it.path("/case-list").build() }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCaseListResponse())
      }
  }

  @Test
  fun `should get cases`() {
    restTestClient.get().uri { builder ->
      builder.path("/cases")
        .queryParam("crns", crns[0], crns[1])
        .build()
    }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCasesResponse())
      }
  }

  @Test
  fun `should get cases with correct filters`() {
    restTestClient.get().uri { builder ->
      builder.path("/cases")
        .queryParam("crns", crns[0], crns[1])
        .queryParam("riskLevel", RiskLevel.MEDIUM.name)
        .build()
    }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCasesWithFilterResponse())
      }
  }

  @Test
  fun `should get cases V2`() {
    restTestClient.get().uri { builder ->
      builder.path("/v2/cases")
        .queryParam("crns", crns[0], crns[1])
        .build()
    }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCasesV2Response())
      }
  }

  @Test
  fun `should get case V2`() {
    restTestClient.get().uri("/v2/cases/${crns[0]}")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCaseV2Response())
      }
  }

  private fun stubInitialCaseSummaries() {
    val summaries = CaseSummaries(
      listOf(
        buildCaseSummary(crn = crns[0], nomsId = nomsNumbers[0]),
        buildCaseSummary(crn = crns[1], nomsId = nomsNumbers[1]),
      ),
    )
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(response = summaries)
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
      noms = nomsNumbers[1],
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
    val caseList = CaseList(
      crns.mapIndexed { i, crn ->
        buildCase(
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
            buildRoshCodeDescription("RMRH", "Medium")
          } else {
            buildRoshCodeDescription()
          },
        )
      },
    )

    ProbationIntegrationSasDeliusStubs.stubGetCaseListByUsername(
      deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
      response = caseList,
    )
  }

  private fun seedCaseEntities() {
    val entities = listOf(
      buildCaseEntity { withCrn(crns[5]) },
      buildCaseEntity(
        tierScore = TierScore.A1S,
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
      ) { withCrn(crns[6]) },
      buildCaseEntity(
        tierScore = TierScore.C1,
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
      ) { withCrn(crns[7]) },
      buildCaseEntity(
        tierScore = TierScore.B3,
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
      ) { withCrn(crns[8]) },
      buildCaseEntity(
        tierScore = TierScore.B3,
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        cas1ApplicationRequestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
        cas1ApplicationPlacementStatus = Cas1PlacementStatus.CANCELLED,
      ) { withCrn(crns[9]) },
      buildCaseEntity(
        tierScore = TierScore.B3,
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
      ) { withCrn(crns[10]) },
      buildCaseEntity(
        tierScore = TierScore.B3,
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.REJECTED,
      ) { withCrn(crns[11]) },
      buildCaseEntity(
        tierScore = TierScore.B3,
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.STARTED,
      ) { withCrn(crns[12]) },
      buildCaseEntity(
        tierScore = null,
        cas1ApplicationId = UUID.randomUUID(),
        cas1ApplicationApplicationStatus = Cas1ApplicationStatus.WITHDRAWN,
      ) { withCrn(crns[13]) },
      buildCaseEntity(
        tierScore = TierScore.D3,
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

    (17..19).forEach { stubCorePersonRecord(crns[it], nomsNumbers[it]) }
  }

  private fun stubAdditionalTierResponses() {
    val tier = buildTier()

    TierStubs.getTierOKResponse(crns[2], tier)
    TierStubs.getTierServerErrorResponse(crns[17])
    TierStubs.getTierServerErrorResponse(crns[18])
    TierStubs.getTierOKResponse(crns[15], tier)
    TierStubs.getTierOKResponse(crns[16], tier)
    TierStubs.getTierOKResponse(crns[19], tier)
  }

  private fun stubSuitableApplications() {
    ApprovedPremisesStubs.getCas1SuitableApplicationOKResponse(
      crns[2],
      buildCas1Application(
        applicationStatus = Cas1ApplicationStatus.AWAITING_PLACEMENT,
        requestForPlacementStatus = Cas1RequestForPlacementStatus.REQUEST_UNSUBMITTED,
      ),
    )

    ApprovedPremisesStubs.getCas1SuitableApplicationOKResponse(
      crns[4],
      buildCas1Application(
        applicationStatus = Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
        requestForPlacementStatus = Cas1RequestForPlacementStatus.AWAITING_MATCH,
      ),
    )

    ApprovedPremisesStubs.getCas1SuitableApplicationOKResponse(
      crns[0],
      buildCas1Application(applicationStatus = Cas1ApplicationStatus.EXPIRED),
    )

    listOf(15, 16, 17, 18).forEach {
      ApprovedPremisesStubs.getCas1SuitableApplicationNotFoundResponse(crns[it])
    }

    ApprovedPremisesStubs.getCas1SuitableApplicationServerErrorResponse(crns[19])
  }

  private fun stubCorePersonRecord(
    crn: String,
    noms: String,
    firstName: String? = null,
    lastName: String? = null,
  ) {
    val record = buildCorePersonRecord(
      identifiers = buildIdentifiers(crns = listOf(crn), prisonNumbers = listOf(noms)),
      firstName = firstName,
      lastName = lastName,
    )

    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn, record)
  }
}
