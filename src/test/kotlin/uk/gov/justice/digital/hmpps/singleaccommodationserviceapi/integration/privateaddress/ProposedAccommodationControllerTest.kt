package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress.response.expectedGetPrivateAddressesResponse
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

class ProposedAccommodationControllerTest : IntegrationTestBase() {
  private val crn = "X371199"

  @BeforeEach
  fun setup() {
    hmppsAuth.stubGrantToken()
  }

  @WithMockAuthUser(roles = ["ROLE_PROBATION"])
  @Test
  fun `should get proposed-accommodation for crn`() {
    val result = mockMvc
      .perform(get("/cases/$crn/proposed-accommodations"))
      .andExpect(status().isOk)
      .andReturn()
      .response
      .contentAsString

    assertThatJson(result).matchesExpectedJson(expectedGetPrivateAddressesResponse())
  }
}
