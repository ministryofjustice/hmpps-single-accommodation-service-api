package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCaseV2Response
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCaseV2WithTimeoutResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCaseV2WithUpstreamFailureResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCasesResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCasesV2Response
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCasesV2WithFilterResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCasesWithFilterResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationOasysStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs

class CaseControllerIT : IntegrationTestBase() {

  private val crn = "FAKECRN1"
  private val crn2 = "FAKECRN2"

  @BeforeEach
  fun setup() {
    val corePersonRecord = buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crn)))
    val corePersonRecord2 =
      buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crn)), firstName = "Zack", lastName = "Smith")
    val caseSummaries = CaseSummaries(listOf(buildCaseSummary(crn = crn), buildCaseSummary(crn = crn2)))
    val roshVeryHigh = buildRoshDetails(rosh = buildRosh(riskChildrenCommunity = RiskLevel.VERY_HIGH))
    val roshMedium = buildRoshDetails(rosh = buildRosh(riskChildrenCommunity = RiskLevel.MEDIUM))
    val tier = buildTier()

    createTestDataSetupUserAndDeliusUser()
    HmppsAuthStubs.stubGrantToken()

    // bulk call
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(response = caseSummaries)

    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn = crn, response = corePersonRecord)
    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn = crn2, response = corePersonRecord2)

    ProbationIntegrationOasysStubs.getRoshOKResponse(crn = crn, roshVeryHigh)
    ProbationIntegrationOasysStubs.getRoshOKResponse(crn = crn2, roshMedium)

    TierStubs.getTierOKResponse(crn = crn, tier)
    TierStubs.getTierOKResponse(crn = crn2, tier)
  }

  @Test
  fun `should get cases`() {
    restTestClient.get().uri { builder ->
      builder.path("/cases")
        .queryParam("crns", crn, crn2)
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
        .queryParam("crns", crn, crn2)
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
        .queryParam("crns", crn, crn2)
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
  fun `should get cases V2 with correct filters`() {
    restTestClient.get().uri { builder ->
      builder.path("/v2/cases")
        .queryParam("crns", crn, crn2)
        .queryParam("riskLevel", RiskLevel.MEDIUM.name)
        .build()
    }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCasesV2WithFilterResponse())
      }
  }

  @Test
  fun `should get case V2`() {
    restTestClient.get().uri("/v2/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCaseV2Response())
      }
  }

  @Test
  fun `should get case V2 with partial success when upstream call fails`() {
    ProbationIntegrationOasysStubs.getRoshServerErrorResponse(crn)

    restTestClient.get().uri("/v2/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCaseV2WithUpstreamFailureResponse())
      }
  }

  @Test
  fun `should get case V2 with partial success when upstream call times out`() {
    ProbationIntegrationOasysStubs.getRoshTimeoutResponse(crn)

    restTestClient.get().uri("/v2/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        // replace the random wiremock port with string PORT so this test doesn't fail due to port number changes
        val normalized = it!!.replace(Regex("localhost:\\d+"), "localhost:PORT")
        assertThatJson(normalized).matchesExpectedJson(expectedGetCaseV2WithTimeoutResponse())
      }
  }
}
