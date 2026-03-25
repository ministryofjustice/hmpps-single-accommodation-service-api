package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

object ProbationIntegrationSasDeliusStubs {

  fun stubGetCaseListByUsername(
    deliusUsername: String,
    response: CaseList,
  ) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/case-list/$deliusUsername"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }
}
