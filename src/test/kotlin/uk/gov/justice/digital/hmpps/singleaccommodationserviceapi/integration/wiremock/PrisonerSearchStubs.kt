package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.JsonHelper.jsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockUtils.resolveWireMockUrl

object PrisonerSearchStubs {

  fun getPrisonerOKResponse(prisonNumber: String, response: Prisoner) {
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

  fun getPrisonerServerErrorResponse(prisonNumber: String): String {
    val path = "/prisoner/$prisonNumber"
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo(path))
        .willReturn(serverError()),
    )
    return resolveWireMockUrl(path)
  }
}
