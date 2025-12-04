package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.objectMapper

class ProbationIntegrationDeliusMockServer : WireMockServer(9990) {

  fun stubPostCaseSummariesOKResponse(body: List<String>, response: CaseSummaries) {
    stubFor(
      WireMock
        .post(WireMock.urlPathEqualTo("/probation-cases/summaries"))
//        .withRequestBody(containing(objectMapper.writeValueAsString(body)))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(objectMapper.writeValueAsString(response)),
        ),
    )
  }
}
