package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles.NomisUserDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.JsonHelper.jsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock

object NomisUserRolesStubs {

  fun stubNomisUserDetailsResponse(
    username: String,
    response: NomisUserDetail,
  ) {
    sasWiremock.stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/user/$username"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }
}
