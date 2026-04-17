package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordAddresses
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

object CorePersonRecordStubs {

  fun getCorePersonRecordOKResponse(crn: String, response: CorePersonRecord) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/person/probation/$crn"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getCorePersonRecordNotFoundResponse(crn: String) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/person/probation/$crn"))
        .willReturn(notFound()),
    )
  }

  fun getCorePersonRecordServerErrorResponse(crn: String) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/person/probation/$crn"))
        .willReturn(serverError()),
    )
  }

  fun getCorePersonRecordTimeoutResponse(crn: String) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/person/probation/$crn"))
        .willReturn(okJson("{}").withFixedDelay(6000)),
    )
  }

  fun getCorePersonRecordAddressesOKResponse(crn: String, response: CorePersonRecordAddresses) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/person/probation/$crn/address"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getCorePersonRecordAddressesErrorResponse(crn: String) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/person/probation/$crn/address"))
        .willReturn(serverError()),
    )
  }
}
