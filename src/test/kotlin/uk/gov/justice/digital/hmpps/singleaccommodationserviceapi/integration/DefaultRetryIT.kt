package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.client.RestClientResponseException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SasAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.SasAndDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

class DefaultRetryIT : IntegrationTestBase() {

  @Autowired
  private lateinit var sasAndDeliusCachingService: SasAndDeliusCachingService

  private val username = "TEST_USER"

  @BeforeEach
  fun setup() {
    HmppsAuthStubs.stubGrantToken()
  }

  @Nested
  inner class GetCaseRetry {
    @Test
    fun `should retry once and then return case when getCase fails first time`() {
      val crn = "crn"
      val scenario = "get-case-retry-success"
      val expectedCase = buildCase(crn = crn)

      sasWiremock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/case/$username/$crn"))
          .inScenario(scenario)
          .whenScenarioStateIs(STARTED)
          .willReturn(WireMock.serverError())
          .willSetStateTo("success"),
      )

      sasWiremock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/case/$username/$crn"))
          .inScenario(scenario)
          .whenScenarioStateIs("success")
          .willReturn(WireMock.okJson(jsonMapper.writeValueAsString(expectedCase))),
      )

      val result = sasAndDeliusCachingService.getCase(username, crn)

      assertThat(result.crn).isEqualTo(crn)
      sasWiremock.verify(
        2,
        WireMock.getRequestedFor(
          WireMock.urlPathEqualTo("/case/$username/$crn"),
        ),
      )
    }

    @Test
    fun `should fail after 3 attempts when getCase fails all times`() {
      SasAndDeliusStubs.stubGetCaseFailure(username, "crn")

      val error = assertThrows<RestClientResponseException> {
        sasAndDeliusCachingService.getCase(username, "crn")
      }

      assertThat(error.statusCode.value()).isEqualTo(500)

      sasWiremock.verify(
        3,
        WireMock.getRequestedFor(
          WireMock.urlPathEqualTo("/case/$username/crn"),
        ),
      )
    }
  }
}
