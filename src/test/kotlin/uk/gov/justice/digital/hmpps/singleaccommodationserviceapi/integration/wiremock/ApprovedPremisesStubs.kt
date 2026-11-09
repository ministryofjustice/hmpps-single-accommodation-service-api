package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.noContent
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1UrlTemplates
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingPremises
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3UrlTemplates
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.JsonHelper.jsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockManager.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockUtils.resolveWireMockUrl

object ApprovedPremisesStubs {
  const val CAS1_APPLICATION_START_URL = "https://cas1-ui/applications/start"
  const val CAS3_REFERRAL_START_URL = "https://cas3-ui/referrals/start"

  /**
   * CAS Endpoints return a 204 instead of a 404 if there is no content
   *
   * Several tests rely on the default WireMock behaviour of returning a
   * 404 if no stub has been configured, but in the case of the CAS
   * endpoints a 404 is treated as an error instead of a 'no data' response
   *
   * This function ensures the default response for these endpoints is
   * 204, ensuring we don't need to explictly configure the 'no data' response
   * in every test relying on it
   */
  fun setupDefaultResponses() {
    getCas1CurrentPremisesDefaultNoContentResponse()
  }

  fun getCas1UrlTemplatesOKResponse(cas1ApplicationStart: String = CAS1_APPLICATION_START_URL) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/url-templates"))
        .willReturn(okJson(jsonMapper.writeValueAsString(Cas1UrlTemplates(cas1ApplicationStart)))),
    )
  }

  fun getCas3UrlTemplatesOKResponse(cas3ReferralStart: String = CAS3_REFERRAL_START_URL) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/url-templates"))
        .willReturn(okJson(jsonMapper.writeValueAsString(Cas3UrlTemplates(cas3ReferralStart)))),
    )
  }

  fun getCas1CurrentPremisesDefaultNoContentResponse() {
    sasWiremock.stubFor(
      get(urlPathMatching("""/cas1/external/cases/[^/]+/premises/current"""))
        .willReturn(noContent()),
    )
  }

  fun getCas1CurrentPremisesOKResponse(crn: String, response: Cas1PremisesSummary) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/premises/current"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getCas3CurrentPremisesOKResponse(crn: String, response: Cas3BookingPremises) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/premises/current"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getCas1CurrentPremisesNoContentResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/premises/current"))
        .willReturn(noContent()),
    )
  }

  fun getCas3CurrentPremisesNoContentResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/premises/current"))
        .willReturn(noContent()),
    )
  }

  fun getCas1CurrentPremisesNotFoundResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/premises/current"))
        .willReturn(notFound()),
    )
  }

  fun getCas3CurrentPremisesNotFoundResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/premises/current"))
        .willReturn(notFound()),
    )
  }

  fun getCas1CurrentPremisesServerErrorResponse(crn: String): String {
    val path = "/cas1/external/cases/$crn/premises/current"
    sasWiremock.stubFor(
      get(urlPathEqualTo(path))
        .willReturn(serverError()),
    )
    return resolveWireMockUrl(path)
  }

  fun getCas3CurrentPremisesServerErrorResponse(crn: String): String {
    val path = "/cas3/external/cases/$crn/premises/current"
    sasWiremock.stubFor(
      get(urlPathEqualTo(path))
        .willReturn(serverError()),
    )
    return resolveWireMockUrl(path)
  }

  fun getCas1CurrentApplicationOKResponse(crn: String, response: Cas1ApplicationV2) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/applications/current"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getCas2SuitableApplicationOKResponse(crn: String, response: Cas2Application) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas2/external/cases/$crn/applications/suitable"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getCas1CurrentApplicationNotFoundResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/applications/current"))
        .willReturn(notFound()),
    )
  }

  fun getCas2SuitableApplicationNotFoundResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas2/external/cases/$crn/applications/suitable"))
        .willReturn(notFound()),
    )
  }

  fun getCas3SuitableApplicationNotFoundResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/applications/suitable"))
        .willReturn(notFound()),
    )
  }

  fun getCas1CurrentApplicationNoContentResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/applications/current"))
        .willReturn(noContent()),
    )
  }

  fun getCas2SuitableApplicationNoContentResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas2/external/cases/$crn/applications/suitable"))
        .willReturn(noContent()),
    )
  }

  fun getCas3SuitableApplicationNoContentResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/applications/suitable"))
        .willReturn(noContent()),
    )
  }

  fun getCas1CurrentApplicationServerErrorResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/applications/current"))
        .willReturn(serverError()),
    )
  }

  fun getCas3SuitableApplicationOKResponse(crn: String, response: Cas3Application) {
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

  fun postCas2ArrivalOKResponse(crn: String, applicationId: String) {
    sasWiremock.stubFor(
      post(urlPathEqualTo("/cas2/external/cases/$crn/applications/$applicationId/arrival"))
        .willReturn(ok()),
    )
  }

  fun postCas2ArrivalNotFoundResponse(crn: String, applicationId: String) {
    sasWiremock.stubFor(
      post(urlPathEqualTo("/cas2/external/cases/$crn/applications/$applicationId/arrival"))
        .willReturn(notFound()),
    )
  }

  fun postCas2ArrivalServerErrorResponse(crn: String, applicationId: String) {
    sasWiremock.stubFor(
      post(urlPathEqualTo("/cas2/external/cases/$crn/applications/$applicationId/arrival"))
        .willReturn(serverError()),
    )
  }
}
