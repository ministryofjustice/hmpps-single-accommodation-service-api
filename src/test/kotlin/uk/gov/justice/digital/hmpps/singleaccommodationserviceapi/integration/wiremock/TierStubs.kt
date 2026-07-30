package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

object TierStubs {

  fun getTierOKResponse(crn: String, response: Tier, delayMs: Int = 0) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/v2/crn/$crn/tier"))
        .willReturn(
          okJson(jsonMapper.writeValueAsString(response))
            .let { if (delayMs > 0) it.withFixedDelay(delayMs) else it },
        ),
    )
  }

  fun getTierServerErrorResponse(crn: String) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/v2/crn/$crn/tier"))
        .willReturn(serverError()),
    )
  }

  fun getTierNotFoundResponse(crn: String) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/v2/crn/$crn/tier"))
        .willReturn(notFound()),
    )
  }

  fun getTierTimeoutResponse(crn: String) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/v2/crn/$crn/tier"))
        .willReturn(okJson("{}").withFixedDelay(6000)),
    )
  }

  fun getTierOKResponseV3(crn: String, response: Tier, delayMs: Int = 0) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/v3/crn/$crn/tier"))
        .willReturn(
          okJson(jsonMapper.writeValueAsString(response))
            .let { if (delayMs > 0) it.withFixedDelay(delayMs) else it },
        ),
    )
  }

  fun getTierServerErrorResponseV3(crn: String) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/v3/crn/$crn/tier"))
        .willReturn(serverError()),
    )
  }

  fun getTierNotFoundResponseV3(crn: String) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/v3/crn/$crn/tier"))
        .willReturn(notFound()),
    )
  }

  fun getTierTimeoutResponseV3(crn: String) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/v3/crn/$crn/tier"))
        .willReturn(okJson("{}").withFixedDelay(6000)),
    )
  }
}
