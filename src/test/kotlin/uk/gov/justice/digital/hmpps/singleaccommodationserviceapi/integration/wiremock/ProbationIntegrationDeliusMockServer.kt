package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.StaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object ProbationIntegrationDeliusMockServer {

  fun postCaseSummariesOKResponse(response: CaseSummaries) {
    sasWiremock.stubFor(
      post(WireMock.urlPathEqualTo("/probation-cases/summaries"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun stubGetStaffByUsername(
    deliusUsername: String,
    response: StaffDetail,
  ) {
    val encodedUsername = URLEncoder.encode(deliusUsername.uppercase(), StandardCharsets.UTF_8)
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/staff/$encodedUsername"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(jsonMapper.writeValueAsString(response)),
        ),
    )
  }
}
