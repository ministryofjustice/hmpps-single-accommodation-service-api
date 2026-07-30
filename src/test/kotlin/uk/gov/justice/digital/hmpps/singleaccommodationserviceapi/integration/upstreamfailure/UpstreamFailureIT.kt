package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedSingleCrnTierServerError
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response.expectedSingleCrnTierTimeout
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.SasAndDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs

class UpstreamFailureIT : IntegrationTestBase() {

  private val crn = "FAKECRN1"

  @BeforeEach
  fun setup() {
    val corePersonRecord = buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crn)))
    val case = buildCase(crn = crn)
    val tier = buildTier()

    createTestDataSetupUserAndDeliusUser()
    HmppsAuthStubs.stubGrantToken()

    SasAndDeliusStubs.stubGetCase(deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER, crn = crn, response = case)
    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn = crn, response = corePersonRecord)
    TierStubs.getTierOKResponse(crn = crn, tier)
  }

  // normalize non-deterministic parts of the failure response (wiremock ports, HTTP client error messages)
  private fun normalizeResponse(json: String) = json
    .replace(Regex("localhost:\\d+"), "localhost:PORT")
    .replace(Regex("""(I/O error on GET request for \\?"http://localhost:PORT/[^"\\]+\\??": )[^"]+"""), "$1Request cancelled")

  @Test
  fun `getCase should return ServerError when CPR returns server error`() {
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)
    restTestClient.get().uri("/cases/$crn")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus()
      .is5xxServerError
  }

  @Test
  fun `getCase should return ServerError when CPR call times out`() {
    CorePersonRecordStubs.getCorePersonRecordTimeoutResponse(crn)
    restTestClient.get().uri("/cases/$crn")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus()
      .is5xxServerError
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
  fun `getCase should return failure when all upstream calls fail`() {
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)
    TierStubs.getTierServerErrorResponse(crn)

    restTestClient.get().uri("/cases/$crn")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus()
      .is5xxServerError
  }
}
