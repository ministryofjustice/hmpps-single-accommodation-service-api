package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper
import java.util.UUID

object TierStubs {

  fun getTierOKResponse(crn: String, response: Tier, delayMs: Int = 0) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/crn/$crn/tier"))
        .willReturn(
          okJson(jsonMapper.writeValueAsString(response))
            .let { if (delayMs > 0) it.withFixedDelay(delayMs) else it },
        ),
    )
  }

  fun getTierFailResponse(crn: String, externalId: UUID? = null) {
    sasWiremock.stubFor(
      WireMock.get(WireMock.urlPathEqualTo("/crn/$crn/tier"))
        .willReturn(notFound()),
    )
  }
}
