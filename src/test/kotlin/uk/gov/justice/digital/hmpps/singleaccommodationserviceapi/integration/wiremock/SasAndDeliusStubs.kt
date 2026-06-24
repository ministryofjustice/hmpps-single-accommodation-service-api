package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PageMetadata
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

object SasAndDeliusStubs {

  fun stubGetCaseListByUsername(
    deliusUsername: String,
    cases: List<Case>,
    pageSize: Int,
  ) {
    val totalPages = Math.ceilDiv(cases.size, pageSize)

    cases.forEachIndexed { page, case ->
      sasWiremock.stubFor(
        get(WireMock.urlPathEqualTo("/case-list/$deliusUsername"))
          .withQueryParam("page", WireMock.equalTo(page.toString()))
          .withQueryParam("size", WireMock.equalTo(pageSize.toString()))
          .willReturn(
            okJson(
              jsonMapper.writeValueAsString(
                CaseList(
                  cases = listOf(case),
                  page = PageMetadata(
                    size = pageSize.toLong(),
                    number = page.toLong(),
                    totalElements = cases.size.toLong(),
                    totalPages = totalPages.toLong(),
                  ),
                ),
              ),
            ),
          ),
      )
    }
  }

  fun stubGetCase(
    deliusUsername: String,
    crn: String,
    response: Case,
  ) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/case/$deliusUsername/$crn"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun stubGetCaseFailure(
    deliusUsername: String,
    crn: String,
  ) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/case/$deliusUsername/$crn"))
        .willReturn(serverError()),
    )
  }

  fun stubGetCaseNotFoundFailure(
    deliusUsername: String,
    crn: String,
  ) {
    sasWiremock.stubFor(
      get(WireMock.urlPathEqualTo("/case/$deliusUsername/$crn"))
        .willReturn(notFound()),
    )
  }
}
