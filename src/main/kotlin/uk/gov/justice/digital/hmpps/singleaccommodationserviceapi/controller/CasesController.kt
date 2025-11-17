package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model.CasesResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model.MockCasesResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mocks.mockUserCases
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.service.CasesService

@RestController
@RequestMapping("/cases")
class CasesController(
  private val casesService: CasesService,
) {

  // /cases (without crn) returns mock data for local development purposes
  @GetMapping(params = ["!crn"])
  fun userMockCasesGet(): ResponseEntity<MockCasesResponse> {
    val cases = mockUserCases()
    return ResponseEntity.ok(MockCasesResponse(cases))
  }

  // /cases?crn=XXX returns real data from AP Delius Context API
  @GetMapping(params = ["crn"])
  fun userCasesGet(
    @RequestParam crn: String?,
  ): ResponseEntity<CasesResponse> {
    if (crn.isNullOrBlank()) {
      return ResponseEntity.badRequest().body(CasesResponse(emptyList()))
    }
    val cases = casesService.getUserCases(crn)
    return ResponseEntity.ok(CasesResponse(cases))
  }
}
