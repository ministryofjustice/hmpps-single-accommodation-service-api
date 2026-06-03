package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

object PrisonerSearchStubs {

  fun getPrisonerOKResponse(prisonNumber: String, response: Any) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/prisoner/$prisonNumber"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getPrisonerNotFoundResponse(prisonNumber: String) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/prisoner/$prisonNumber"))
        .willReturn(notFound()),
    )
  }

  fun getPrisonerServerErrorResponse(prisonNumber: String) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/prisoner/$prisonNumber"))
        .willReturn(serverError()),
    )
  }
}
