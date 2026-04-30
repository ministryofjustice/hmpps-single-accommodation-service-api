package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FullCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesandoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCaseListResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCaseResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationOasysStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.SasAndDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import java.util.UUID

class CaseControllerIT : IntegrationTestBase() {
  @Autowired
  private lateinit var dutyToReferRepository: DutyToReferRepository

  @Autowired
  private lateinit var caseRepository: CaseRepository

  private val crns = (1..20).map { "FAKECRN$it" }
  private val nomsNumbers = (1..20).map { "PRI$it" }

  @BeforeEach
  fun setup() {
    dutyToReferRepository.deleteAll()
    caseRepository.deleteAll()

    createTestDataSetupUserAndDeliusUser()
    HmppsAuthStubs.stubGrantToken()

    stubInitialCorePersonRecords()
    stubInitialRoshAndTier()
  }

  @Test
  fun `matches a case by all identifiers from CorePersonRecord and adds missing ones`() {
    // case 1 identifiers
    val knownCrnForCase1 = UUID.randomUUID().toString()
    val unknownCrnForCase1 = UUID.randomUUID().toString()
    val unknownPrisonNumberForCase1 = UUID.randomUUID().toString()

    // case 2 identifiers
    val knownPrisonNumberForCase2 = UUID.randomUUID().toString()
    val unknownCrnForCase2 = UUID.randomUUID().toString()

    val case1 = buildCaseEntity { withCrn(knownCrnForCase1) }
    val case2 = buildCaseEntity {
      withCrn(unknownCrnForCase2)
      withPrisonNumber(knownPrisonNumberForCase2)
    }
    val cases = listOf(case1, case2)

    caseRepository.saveAllAndFlush(cases)

    // currently crn is not nullable in dtos, so doing this way to avoid refactoring but add coverage
    caseRepository.findByPrisonNumber(knownPrisonNumberForCase2)!!
      .also { entity ->
        entity.caseIdentifiers.removeIf {
          it.identifierType == IdentifierType.CRN
        }
        caseRepository.saveAndFlush(entity)
      }

    // case list return should not match any persisted CRNs
    val caseList = CaseList(
      listOf(
        buildCase(crn = unknownCrnForCase1, nomsNumber = null),
        buildCase(crn = unknownCrnForCase2, nomsNumber = knownPrisonNumberForCase2),
      ),
    )

    SasAndDeliusStubs.stubGetCaseListByUsername(
      deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
      response = caseList,
    )

    // returns the unknown CRN from the caselist, and the persisted CRN for the case
    stubCorePersonRecord(
      crn = unknownCrnForCase1,
      prisonNumber = unknownPrisonNumberForCase1,
      additionalCrns = listOf(knownCrnForCase1),
    )

    // returns the CRN from the case list, and the persisted Prison Number for the case
    stubCorePersonRecord(
      crn = unknownCrnForCase2,
      prisonNumber = knownPrisonNumberForCase2,
    )

    restTestClient.get().uri { it.path("/case-list").build() }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody()
      .jsonPath("$.data.length()").isEqualTo(2)

    assertThat(caseRepository.findByCrn(unknownCrnForCase1)!!.caseIdentifiers)
      .extracting<String> { it.identifier }
      .containsExactlyInAnyOrder(
        knownCrnForCase1,
        unknownCrnForCase1,
        unknownPrisonNumberForCase1,
      )

    assertThat(caseRepository.findByPrisonNumber(knownPrisonNumberForCase2)!!.caseIdentifiers)
      .extracting<String> { it.identifier }
      .containsExactlyInAnyOrder(
        knownPrisonNumberForCase2,
        unknownCrnForCase2,
      )
  }

  @Test
  fun `should get successful response from case-list`() {
    stubCaseList()
    seedCaseEntities()
    stubAdditionalCorePersonRecords()

    restTestClient.get().uri { it.path("/case-list").build() }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCaseListResponse())
      }
  }

  @Test
  fun `should filter cases based on risk level`() {
    stubCaseList()
    seedCaseEntities()
    stubAdditionalCorePersonRecords()

    val riskLevels = listOf(
      "VERY_HIGH" to 19,
      "MEDIUM" to 1,
      "LOW" to 0,
      "" to 20,
    )

    riskLevels.forEach { riskLevel ->
      val response = restTestClient.get().uri {
        it.path("/case-list")
          .queryParam("riskLevel", riskLevel.first).build()
      }
        .withDeliusUserJwt()
        .exchangeSuccessfully()

      response.expectBody(object : ParameterizedTypeReference<ApiResponseDto<List<FullCaseDto>>>() {})
        .value { responseDto ->
          assertThat(responseDto!!.data.map { it.riskLevel?.name })
            .hasSize(riskLevel.second)
            .allMatch { riskLevel.first.isEmpty() || it == riskLevel.first }
        }
    }
  }

  @Test
  fun `should filter cases based on search term`() {
    stubCaseList()
    seedCaseEntities()
    stubAdditionalCorePersonRecords()

    val searchParams = listOf(
      "AAAAA" to 0,
      "" to 20,
      "FAKECRN1" to 1,
      "FIR" to 19,
      "first" to 19,
      "FiRsT M" to 19,
      "FiRsT M Last" to 0,
      "Zack" to 1,
    )

    searchParams.forEach { param ->
      println("$param  ****")
      restTestClient.get().uri {
        it.path("/case-list")
          .queryParam("searchTerm", param.first).build()
      }
        .withDeliusUserJwt()
        .exchangeSuccessfully()
        .expectBody()
        .jsonPath("$.data.length()").isEqualTo(param.second)
    }
  }

  @Test
  fun `should get case`() {
    val case = buildCase(crn = crns[0], nomsNumber = nomsNumbers[0])
    SasAndDeliusStubs.stubGetCase(deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER, crn = case.crn, response = case)
    restTestClient.get().uri("/cases/${crns[0]}")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCaseResponse())
      }
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
            buildRoshLevel("RMRH", "Medium")
          } else {
            buildRoshLevel()
          },
        )
      },
    )

    SasAndDeliusStubs.stubGetCaseListByUsername(
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
