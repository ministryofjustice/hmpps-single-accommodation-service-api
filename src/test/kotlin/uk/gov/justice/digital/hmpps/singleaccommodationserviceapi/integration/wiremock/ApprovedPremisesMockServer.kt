package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas2v2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.objectMapper

class ApprovedPremisesMockServer : WireMockServer(9992) {

  fun stubGetSuitableApplicationOKResponse(crn: String, response: Any) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/cas1/external/suitable-application/$crn"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(objectMapper.writeValueAsString(response)),
        ),
    )
  }

  fun stubGetCas1ReferralOKResponse(crn: String, response: List<Cas1ReferralHistory>) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/cas1/external/referrals/$crn"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(objectMapper.writeValueAsString(response)),
        ),
    )
  }

  fun stubGetCas2ReferralOKResponse(crn: String, response: List<Cas2ReferralHistory>) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/cas2/external/referrals/$crn"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(objectMapper.writeValueAsString(response)),
        ),
    )
  }

  fun stubGetCas2v2ReferralOKResponse(crn: String, response: List<Cas2v2ReferralHistory>) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/cas2v2/external/referrals/$crn"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(objectMapper.writeValueAsString(response)),
        ),
    )
  }

  fun stubGetCas3ReferralOKResponse(crn: String, response: List<Cas3ReferralHistory>) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/cas3/external/referrals/$crn"))
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
