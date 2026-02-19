package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper
import java.util.UUID

class TierMockServer : WireMockServer(9994) {

  fun stubGetCorePersonRecordOKResponse(crn: String, response: Tier, delayMs: Int = 0) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/crn/$crn/tier"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(jsonMapper.writeValueAsString(response))
            .let { if (delayMs > 0) it.withFixedDelay(delayMs) else it },
        ),
    )
  }

  fun stubGetTierFailResponse(crn: String, externalId: UUID? = null) {
    stubFor(
      WireMock.get(WireMock.urlPathEqualTo("/crn/$crn/tier"))
        .willReturn(
          WireMock.aResponse().withStatus(HttpStatus.NOT_FOUND.value()),
        ),
    )
  }
}
