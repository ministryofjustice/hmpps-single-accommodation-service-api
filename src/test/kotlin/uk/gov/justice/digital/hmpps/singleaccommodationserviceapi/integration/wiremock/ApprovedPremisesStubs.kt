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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementV2
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
    getCas3CurrentPremisesDefaultNoContentResponse()
    getCas1ResidenceDefaultNoContentResponse()
    getCas2CurrentApplicationDefaultNoContentResponse()
    getCas3CurrentApplicationDefaultNoContentResponse()
    getCas1CurrentApplicationsDefaultNoContentResponse()
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

  fun getCas3CurrentPremisesDefaultNoContentResponse() {
    sasWiremock.stubFor(
      get(urlPathMatching("""/cas3/external/cases/[^/]+/premises/current"""))
        .willReturn(noContent()),
    )
  }

  fun getCas2CurrentApplicationDefaultNoContentResponse() {
    sasWiremock.stubFor(
      get(urlPathMatching("""/cas2/external/cases/[^/]+/applications/current"""))
        .willReturn(noContent()),
    )
  }

  fun getCas3CurrentApplicationDefaultNoContentResponse() {
    sasWiremock.stubFor(
      get(urlPathMatching("""/cas3/external/cases/[^/]+/applications/current"""))
        .willReturn(noContent()),
    )
  }

  fun getCas1CurrentApplicationsDefaultNoContentResponse() {
    sasWiremock.stubFor(
      get(urlPathMatching("""/cas1/external/cases/[^/]+/applications/current"""))
        .willReturn(noContent()),
    )
  }

  fun getCas1ResidenceOKResponse(crn: String, response: Cas1PlacementV2) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/residence"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getCas1ResidenceDefaultNoContentResponse() {
    sasWiremock.stubFor(
      get(urlPathMatching("""/cas1/external/cases/[^/]+/residence"""))
        .willReturn(noContent()),
    )
  }
  fun getCas1ResidenceNoContentResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/residence"))
        .willReturn(noContent()),
    )
  }

  fun getCas1ResidenceNotFoundResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/residence"))
        .willReturn(notFound()),
    )
  }

  fun getCas1ResidenceServerErrorResponse(crn: String): String {
    val path = "/cas1/external/cases/$crn/residence"
    sasWiremock.stubFor(
      get(urlPathEqualTo(path))
        .willReturn(serverError()),
    )
    return resolveWireMockUrl(path)
  }

  fun getCas3CurrentPremisesOKResponse(crn: String, response: Cas3BookingPremises) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/premises/current"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getCas3CurrentPremisesNoContentResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/premises/current"))
        .willReturn(noContent()),
    )
  }

  fun getCas3CurrentPremisesNotFoundResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/premises/current"))
        .willReturn(notFound()),
    )
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

  fun getCas2CurrentApplicationOKResponse(crn: String, response: Cas2Application) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas2/external/cases/$crn/applications/current"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun getCas1CurrentApplicationNotFoundResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/applications/current"))
        .willReturn(notFound()),
    )
  }

  fun getCas2CurrentApplicationNotFoundResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas2/external/cases/$crn/applications/current"))
        .willReturn(notFound()),
    )
  }

  fun getCas3CurrentApplicationNotFoundResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/applications/current"))
        .willReturn(notFound()),
    )
  }

  fun getCas1CurrentApplicationNoContentResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/applications/current"))
        .willReturn(noContent()),
    )
  }

  fun getCas2CurrentApplicationNoContentResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas2/external/cases/$crn/applications/current"))
        .willReturn(noContent()),
    )
  }

  fun getCas3CurrentApplicationNoContentResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/applications/current"))
        .willReturn(noContent()),
    )
  }

  fun getCas1CurrentApplicationServerErrorResponse(crn: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas1/external/cases/$crn/applications/current"))
        .willReturn(serverError()),
    )
  }

  fun getCas3CurrentApplicationOKResponse(crn: String, response: Cas3Application) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/cas3/external/cases/$crn/applications/current"))
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
