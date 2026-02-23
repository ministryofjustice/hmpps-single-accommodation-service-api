package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

object CorePersonRecordStubs {

  fun getCorePersonRecordOKResponse(crn: String, response: CorePersonRecord) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/person/probation/$crn"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }
}
