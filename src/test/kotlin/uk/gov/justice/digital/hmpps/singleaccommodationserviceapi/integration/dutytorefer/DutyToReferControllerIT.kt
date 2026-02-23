package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs

class DutyToReferControllerIT : IntegrationTestBase() {
  private val crn = "FAKECRN1"

  @BeforeEach
  fun setup() {
    HmppsAuthStubs.stubGrantToken()
  }

  @Test
  fun `should get dutyToRefers for crn`() {
    restTestClient.get().uri("/cases/{crn}/dtrs", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully().expectStatus().isOk
  }
}
