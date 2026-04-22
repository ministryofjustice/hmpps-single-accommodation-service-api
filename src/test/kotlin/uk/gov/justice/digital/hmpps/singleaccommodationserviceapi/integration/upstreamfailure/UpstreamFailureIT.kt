package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedSingleCrnAllUpstreamFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedSingleCrnCprServerError
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedSingleCrnCprTimeout
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedSingleCrnRoshServerError
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedSingleCrnRoshTimeout
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedSingleCrnTierServerError
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedSingleCrnTierTimeout
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationOasysStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs

class UpstreamFailureIT : IntegrationTestBase() {

  private val crn = "FAKECRN1"

  @BeforeEach
  fun setup() {
    val corePersonRecord = buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crn)))
    val caseSummaries = CaseSummaries(listOf(buildCaseSummary(crn = crn)))
    val roshVeryHigh = buildRoshDetails(rosh = buildRosh(riskChildrenCommunity = RiskLevel.VERY_HIGH))
    val tier = buildTier()

    createTestDataSetupUserAndDeliusUser()
    HmppsAuthStubs.stubGrantToken()

    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(response = caseSummaries)
    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn = crn, response = corePersonRecord)
    ProbationIntegrationOasysStubs.getRoshOKResponse(crn = crn, roshVeryHigh)
    TierStubs.getTierOKResponse(crn = crn, tier)
  }

  // normalize non-deterministic parts of the failure response (wiremock ports, HTTP client error messages)
  private fun normalizeResponse(json: String) = json
    .replace(Regex("localhost:\\d+"), "localhost:PORT")
    .replace(Regex("""(I/O error on GET request for \\?"http://localhost:PORT/[^"\\]+\\??": )[^"]+"""), "$1Request cancelled")

  @Test
  fun `getCase should return partial success when CPR call returns server error`() {
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)

    restTestClient.get().uri("/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedSingleCrnCprServerError())
      }
  }

  @Test
  fun `getCase should return partial success when CPR call times out`() {
    CorePersonRecordStubs.getCorePersonRecordTimeoutResponse(crn)

    restTestClient.get().uri("/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(normalizeResponse(it!!)).matchesExpectedJson(expectedSingleCrnCprTimeout())
      }
  }

  @Test
  fun `getCase should return partial success when ROSH call returns server error`() {
    ProbationIntegrationOasysStubs.getRoshServerErrorResponse(crn)

    restTestClient.get().uri("/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedSingleCrnRoshServerError())
      }
  }

  @Test
  fun `getCase should return partial success when ROSH call times out`() {
    ProbationIntegrationOasysStubs.getRoshTimeoutResponse(crn)

    restTestClient.get().uri("/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(normalizeResponse(it!!)).matchesExpectedJson(expectedSingleCrnRoshTimeout())
      }
  }

  @Test
  fun `getCase should return partial success when Tier call returns server error`() {
    TierStubs.getTierServerErrorResponse(crn)

    restTestClient.get().uri("/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedSingleCrnTierServerError())
      }
  }

  @Test
  fun `getCase should return partial success when Tier call times out`() {
    TierStubs.getTierTimeoutResponse(crn)

    restTestClient.get().uri("/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(normalizeResponse(it!!)).matchesExpectedJson(expectedSingleCrnTierTimeout())
      }
  }

  @Test
  fun `getCase should return partial success when all upstream calls fail`() {
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)
    ProbationIntegrationOasysStubs.getRoshServerErrorResponse(crn)
    TierStubs.getTierServerErrorResponse(crn)

    restTestClient.get().uri("/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedSingleCrnAllUpstreamFailures())
      }
  }
}
