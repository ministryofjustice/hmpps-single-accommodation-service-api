package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

class PrisonerSearchMockServer : WireMockServer(9995) {

  fun stubGetPrisonerOKResponse(prisonerNumber: String, response: Any) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/prisoner/$prisonerNumber"))
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
