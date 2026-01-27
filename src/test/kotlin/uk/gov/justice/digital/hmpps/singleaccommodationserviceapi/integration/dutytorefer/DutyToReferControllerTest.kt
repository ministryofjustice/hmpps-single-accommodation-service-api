package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase

class DutyToReferControllerTest : IntegrationTestBase() {
  private val crn = "FAKECRN1"

  @BeforeEach
  fun setup() {
    hmppsAuth.stubGrantToken()
  }

  @Test
  fun `should get dutyToRefers for crn`() {
    restTestClient.get().uri("/cases/{crn}/dtrs", crn)
      .withJwt()
      .exchangeSuccessfully().expectStatus().isOk
  }
}
