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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedMultiCrnAllUpstreamFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedMultiCrnCprServerError
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedMultiCrnCprTimeout
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedMultiCrnRoshServerError
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedMultiCrnRoshTimeout
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedMultiCrnTierNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedMultiCrnTierServerError
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
  private val crn2 = "FAKECRN2"

  @BeforeEach
  fun setup() {
    val corePersonRecord = buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crn)))
    val corePersonRecord2 =
      buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crn2)), firstName = "Zack", lastName = "Smith")
    val caseSummaries = CaseSummaries(listOf(buildCaseSummary(crn = crn), buildCaseSummary(crn = crn2)))
    val roshVeryHigh = buildRoshDetails(rosh = buildRosh(riskChildrenCommunity = RiskLevel.VERY_HIGH))
    val roshMedium = buildRoshDetails(rosh = buildRosh(riskChildrenCommunity = RiskLevel.MEDIUM))
    val tier = buildTier()

    createTestDataSetupUserAndDeliusUser()
    HmppsAuthStubs.stubGrantToken()

    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(response = caseSummaries)

    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn = crn, response = corePersonRecord)
    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn = crn2, response = corePersonRecord2)

    ProbationIntegrationOasysStubs.getRoshOKResponse(crn = crn, roshVeryHigh)
    ProbationIntegrationOasysStubs.getRoshOKResponse(crn = crn2, roshMedium)

    TierStubs.getTierOKResponse(crn = crn, tier)
    TierStubs.getTierOKResponse(crn = crn2, tier)
  }

  // normalize non-deterministic parts of the failure response (wiremock ports, HTTP client error messages)
  private fun normalizeResponse(json: String) = json
    .replace(Regex("localhost:\\d+"), "localhost:PORT")
    .replace(Regex("""(I/O error on GET request for \\?"http://localhost:PORT/[^"\\]+\\??": )[^"]+"""), "$1Request cancelled")

  @Test
  fun `getCaseV2 should return partial success when CPR call returns server error`() {
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)

    restTestClient.get().uri("/v2/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedSingleCrnCprServerError())
      }
  }

  @Test
  fun `getCaseV2 should return partial success when CPR call times out`() {
    CorePersonRecordStubs.getCorePersonRecordTimeoutResponse(crn)

    restTestClient.get().uri("/v2/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(normalizeResponse(it!!)).matchesExpectedJson(expectedSingleCrnCprTimeout())
      }
  }

  @Test
  fun `getCaseV2 should return partial success when ROSH call returns server error`() {
    ProbationIntegrationOasysStubs.getRoshServerErrorResponse(crn)

    restTestClient.get().uri("/v2/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedSingleCrnRoshServerError())
      }
  }

  @Test
  fun `getCaseV2 should return partial success when ROSH call times out`() {
    ProbationIntegrationOasysStubs.getRoshTimeoutResponse(crn)

    restTestClient.get().uri("/v2/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(normalizeResponse(it!!)).matchesExpectedJson(expectedSingleCrnRoshTimeout())
      }
  }

  @Test
  fun `getCaseV2 should return partial success when Tier call returns server error`() {
    TierStubs.getTierServerErrorResponse(crn)

    restTestClient.get().uri("/v2/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedSingleCrnTierServerError())
      }
  }

  @Test
  fun `getCaseV2 should return partial success when Tier call times out`() {
    TierStubs.getTierTimeoutResponse(crn)

    restTestClient.get().uri("/v2/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(normalizeResponse(it!!)).matchesExpectedJson(expectedSingleCrnTierTimeout())
      }
  }

  @Test
  fun `getCaseV2 should return partial success when all upstream calls fail`() {
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)
    ProbationIntegrationOasysStubs.getRoshServerErrorResponse(crn)
    TierStubs.getTierServerErrorResponse(crn)

    restTestClient.get().uri("/v2/cases/$crn")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedSingleCrnAllUpstreamFailures())
      }
  }

  @Test
  fun `getCasesV2 should return partial success when CPR call returns server error for one CRN`() {
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)

    restTestClient.get().uri { builder ->
      builder.path("/v2/cases")
        .queryParam("crns", crn, crn2)
        .build()
    }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedMultiCrnCprServerError())
      }
  }

  @Test
  fun `getCasesV2 should return partial success when CPR call times out for one CRN`() {
    CorePersonRecordStubs.getCorePersonRecordTimeoutResponse(crn)

    restTestClient.get().uri { builder ->
      builder.path("/v2/cases")
        .queryParam("crns", crn, crn2)
        .build()
    }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(normalizeResponse(it!!)).matchesExpectedJson(expectedMultiCrnCprTimeout())
      }
  }

  @Test
  fun `getCasesV2 should return partial success when ROSH call returns server error for one CRN`() {
    ProbationIntegrationOasysStubs.getRoshServerErrorResponse(crn)

    restTestClient.get().uri { builder ->
      builder.path("/v2/cases")
        .queryParam("crns", crn, crn2)
        .build()
    }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedMultiCrnRoshServerError())
      }
  }

  @Test
  fun `getCasesV2 should return partial success when ROSH call times out for one CRN`() {
    ProbationIntegrationOasysStubs.getRoshTimeoutResponse(crn)

    restTestClient.get().uri { builder ->
      builder.path("/v2/cases")
        .queryParam("crns", crn, crn2)
        .build()
    }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(normalizeResponse(it!!)).matchesExpectedJson(expectedMultiCrnRoshTimeout())
      }
  }

  @Test
  fun `getCasesV2 should return partial success when Tier call returns server error for one CRN`() {
    TierStubs.getTierServerErrorResponse(crn)

    restTestClient.get().uri { builder ->
      builder.path("/v2/cases")
        .queryParam("crns", crn, crn2)
        .build()
    }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedMultiCrnTierServerError())
      }
  }

  @Test
  fun `getCasesV2 should return partial success when Tier call returns not found for one CRN`() {
    TierStubs.getTierFailResponse(crn)

    restTestClient.get().uri { builder ->
      builder.path("/v2/cases")
        .queryParam("crns", crn, crn2)
        .build()
    }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedMultiCrnTierNotFound())
      }
  }

  @Test
  fun `getCasesV2 should return partial success with all failures when all upstream calls fail for all CRNs`() {
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn2)
    ProbationIntegrationOasysStubs.getRoshServerErrorResponse(crn)
    ProbationIntegrationOasysStubs.getRoshServerErrorResponse(crn2)
    TierStubs.getTierServerErrorResponse(crn)
    TierStubs.getTierServerErrorResponse(crn2)

    restTestClient.get().uri { builder ->
      builder.path("/v2/cases")
        .queryParam("crns", crn, crn2)
        .build()
    }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedMultiCrnAllUpstreamFailures())
      }
  }
}
