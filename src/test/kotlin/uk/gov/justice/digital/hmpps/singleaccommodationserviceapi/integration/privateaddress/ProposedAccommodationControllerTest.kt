package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress.response.expectedGetPrivateAddressesResponse

class ProposedAccommodationControllerTest : IntegrationTestBase() {
  private val crn = "X371199"

  @BeforeEach
  fun setup() {
    hmppsAuth.stubGrantToken()
  }

  @Test
  fun `should get proposed-accommodation for crn`() {
    restTestClient.get().uri("/cases/{crn}/proposed-accommodations", crn)
      .withJwt()
      .exchange().expectStatus().isOk
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetPrivateAddressesResponse())
      }
  }
}
