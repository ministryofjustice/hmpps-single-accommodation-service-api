package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.PRISONER_SEARCH_RESPONSE_FIELDS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.JsonHelper.jsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock

object PrisonerSearchStubs {

  fun getPrisonerOKResponse(prisonNumber: String, response: Prisoner) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/prisoner/$prisonNumber?responseFields=$PRISONER_SEARCH_RESPONSE_FIELDS"))
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
