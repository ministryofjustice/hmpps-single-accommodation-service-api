package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.case

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cases")
class CaseController(
  val caseService: CaseService,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping
  fun getCases(@RequestBody crns: List<String>): ResponseEntity<CaseSummaries> {
    val cases = caseService.getCases(crns)
    return ResponseEntity.ok(cases)
  }
}
