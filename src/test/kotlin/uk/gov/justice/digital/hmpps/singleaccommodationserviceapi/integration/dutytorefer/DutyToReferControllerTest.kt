package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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

  @WithMockAuthUser(roles = ["ROLE_PROBATION"])
  @Test
  fun `should get dutyToRefers for crn`() {
    val result = mockMvc
      .perform(get("/cases/$crn/dtrs"))
      .andExpect(status().isOk)
      .andReturn()
      .response
      .contentAsString

    assertThatJson(result).matchesExpectedJson(expectedGetDutyToRefersResponse(crn))
  }
}
