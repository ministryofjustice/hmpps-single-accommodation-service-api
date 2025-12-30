package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.mock.mockCrns
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress.response.expectedGetPrivateAddressesResponse
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

class PrivateAddressControllerTest : IntegrationTestBase() {
  private val crn = mockCrns.first()

  @BeforeEach
  fun setup() {
    hmppsAuth.stubGrantToken()
  }

  @WithMockAuthUser(roles = ["ROLE_PROBATION"])
  @Test
  fun `should get private addresses for crn`() {
    val result = mockMvc
      .perform(get("/private-addresses/$crn"))
      .andExpect(status().isOk)
      .andReturn()
      .response
      .contentAsString

    assertThatJson(result).matchesExpectedJson(expectedGetPrivateAddressesResponse(crn))
  }
}
