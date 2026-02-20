package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

object ProbationIntegrationOasysStubs {

  fun getRoshOKResponse(crn: String, response: RoshDetails) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/rosh/$crn"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }
}
