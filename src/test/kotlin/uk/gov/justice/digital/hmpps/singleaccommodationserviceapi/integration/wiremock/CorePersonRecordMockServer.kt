package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.objectMapper

class CorePersonRecordMockServer : WireMockServer(9993) {

  fun stubGetCorePersonRecordOKResponse(crn: String, response: CorePersonRecord) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/person/probation/$crn"))
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
