package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

object CommissionedRehabilitativeServicesStubs {

  fun getCrsOkResponse(crn: String, response: CommissionedRehabilitativeServices) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/crs/$crn"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }
}
