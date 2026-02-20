package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

object ApprovedPremisesStubs {

  fun getSuitableApplicationOKResponse(crn: String, response: Any) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/applications/suitable"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getReferralOKResponse(
    casService: CasService,
    crn: String,
    response: List<ReferralHistory<CasStatus>>,
  ) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/${casService.name.lowercase()}/external/referrals/$crn"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }
}
