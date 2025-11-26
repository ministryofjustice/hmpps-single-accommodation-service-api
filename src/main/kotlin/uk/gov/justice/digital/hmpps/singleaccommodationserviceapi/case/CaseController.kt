package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.case

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CaseController(
  val caseService: CaseService,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases")
  fun getCases(@RequestParam crns: List<String> = emptyList()): ResponseEntity<List<Case>> {
    val cases = caseService.getCases(crns)
    return ResponseEntity.ok(cases)
  }

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/case/{crn}")
  fun getCase(@PathVariable crn: String): ResponseEntity<Any> {
    val cases = caseService.getCase(crn)
    return ResponseEntity.ok(cases)
  }
}
