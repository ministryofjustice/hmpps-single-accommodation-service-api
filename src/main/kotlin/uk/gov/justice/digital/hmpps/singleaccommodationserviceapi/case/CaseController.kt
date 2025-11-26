package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.case

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.response.SasResponse

@RestController
class CaseController(
  val caseService: CaseService,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases")
  fun getCases(
    @RequestParam crns: List<String> = listOf(
      "X371199",
      "X968879",
      "X966926",
      "X969031",
    ),
  ): ResponseEntity<SasResponse<List<Case>>> {
    val cases = caseService.getCases(crns)
    return ResponseEntity.ok(SasResponse(cases))
  }

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/case/{crn}")
  fun getCase(@PathVariable crn: String): ResponseEntity<SasResponse<Case>> {
    val case = caseService.getCase(crn)
    return ResponseEntity.ok(SasResponse(case))
  }
}
