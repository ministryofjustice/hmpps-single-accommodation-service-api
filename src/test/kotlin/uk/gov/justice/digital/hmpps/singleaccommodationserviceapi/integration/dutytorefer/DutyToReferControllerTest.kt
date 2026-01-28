package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.response.expectedGetDutyToRefersResponse
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

class DutyToReferControllerTest : IntegrationTestBase() {
  private val crn = "X371199"

  @BeforeEach
  fun setup() {
    hmppsAuth.stubGrantToken()
  }

  @Test
  fun `should get dutyToRefers for crn`() {
    restTestClient.get().uri("/cases/{crn}/dtrs", crn)
      .withJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetDutyToRefersResponse(crn))
      }
  }
}
