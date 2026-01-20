package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.objectMapper

class ApprovedPremisesMockServer : WireMockServer(9992) {

  fun stubGetSuitableApplicationOKResponse(crn: String, response: Any) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/cas1/external/suitable-applications/$crn"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(objectMapper.writeValueAsString(response)),
        ),
    )
  }

  fun stubGetSuitableCas3ApplicationOKResponse(crn: String, response: Any) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/cas3/external/suitable-applications/$crn"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(objectMapper.writeValueAsString(response)),
        ),
    )
  }

  fun stubGetReferralOKResponse(
    casService: CasService,
    crn: String,
    response: List<ReferralHistory<CasStatus>>,
  ) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/${casService.name.lowercase()}/external/referrals/$crn"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(objectMapper.writeValueAsString(response)),
        ),
    )
  }
}
