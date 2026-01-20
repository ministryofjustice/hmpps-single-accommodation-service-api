package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.Accommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.Crs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.DutyToRefer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.objectMapper

class AccommodationDataDomainMockServer : WireMockServer(9996) {

  fun stubGetDutyToReferOKResponse(crn: String, response: DutyToRefer) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/cases/$crn/duty-to-refers"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(objectMapper.writeValueAsString(response)),
        ),
    )
  }

  fun stubGetCrsOKResponse(crn: String, response: Crs) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/cases/$crn/crs"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(objectMapper.writeValueAsString(response)),
        ),
    )
  }

  fun stubGetCurrentAccommodationOKResponse(response: Accommodation) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/current-accommodation"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.OK.value())
            .withBody(objectMapper.writeValueAsString(response)),
        ),
    )
  }

  fun stubGetProposedAccommodationsOKResponse(response: List<Accommodation>) {
    stubFor(
      WireMock
        .get(WireMock.urlPathEqualTo("/proposed-accommodations"))
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
