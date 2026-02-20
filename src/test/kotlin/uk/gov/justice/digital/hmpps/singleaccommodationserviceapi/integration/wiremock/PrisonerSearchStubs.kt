package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

object PrisonerSearchStubs {

  fun getPrisonerOKResponse(prisonerNumber: String, response: Any) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/prisoner/$prisonerNumber"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }
}
