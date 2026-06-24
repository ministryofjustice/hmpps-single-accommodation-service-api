package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

object ApprovedPremisesStubs {
  fun getCas1CurrentPremisesOKResponse(crn: String, response: Any) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/premises/current"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getCas3CurrentPremisesOKResponse(crn: String, response: Any) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/premises/current"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getCas1CurrentPremisesServerErrorResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/premises/current"))
        .willReturn(serverError()),
    )
  }

  fun getCas3CurrentPremisesServerErrorResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/premises/current"))
        .willReturn(serverError()),
    )
  }

  fun getCas1SuitableApplicationOKResponse(crn: String, response: Any) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/applications/suitable"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getCas1SuitableApplicationNotFoundResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/applications/suitable"))
        .willReturn(notFound()),
    )
  }

  fun getCas3SuitableApplicationNotFoundResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/applications/suitable"))
        .willReturn(notFound()),
    )
  }

  fun getCas1SuitableApplicationServerErrorResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/applications/suitable"))
        .willReturn(serverError()),
    )
  }

  fun getCas3SuitableApplicationOKResponse(crn: String, response: Any) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/applications/suitable"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getReferralOKResponse(
    casService: CasService,
    crn: String,
    response: List<CasReferralHistory>,
  ) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/${casService.urlPath}/external/referrals/$crn"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }
}
