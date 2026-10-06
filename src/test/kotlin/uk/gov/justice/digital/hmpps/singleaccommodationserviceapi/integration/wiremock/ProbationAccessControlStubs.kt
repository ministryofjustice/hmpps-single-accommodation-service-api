package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.CaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.UserCaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.JsonHelper.jsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock

object ProbationAccessControlStubs {

  fun postUserAccessOKResponse(username: String, response: UserCaseAccess) {
    sasWiremock.stubFor(
      post(WireMock.urlPathEqualTo("/user/$username/access"))
        .willReturn(okJson(jsonMapper.writeValueAsString(response))),
    )
  }

  fun postUserAccessForCrns(
    username: String,
    accessibleCrns: List<String> = emptyList(),
    excludedCrns: List<String> = emptyList(),
    restrictedCrns: List<String> = emptyList(),
  ) = postUserAccessOKResponse(
    username,
    UserCaseAccess(
      accessibleCrns.map { CaseAccess(crn = it, userExcluded = false, userRestricted = false) } +
        excludedCrns.map { CaseAccess(crn = it, userExcluded = true, userRestricted = false, exclusionMessage = "excluded") } +
        restrictedCrns.map { CaseAccess(crn = it, userExcluded = false, userRestricted = true, restrictionMessage = "restricted") },
    ),
  )

  fun postUserAccessServerError(username: String) {
    sasWiremock.stubFor(
      post(WireMock.urlPathEqualTo("/user/$username/access"))
        .willReturn(serverError()),
    )
  }
}
