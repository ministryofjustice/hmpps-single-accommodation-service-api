package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel

@RestController
class CaseController(
  val caseService: CaseService,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases")
  fun getCases(
    @RequestParam(required = false) searchTerm: String?,
    @RequestParam(required = false) status: Status?,
    @RequestParam(required = false) assignedTo: Long?,
    @RequestParam(required = false) riskLevel: RiskLevel?,
    // these crns are added temporarily
    @RequestParam(required = false) crns: List<String> = listOf(
      "X371199",
      "X968879",
      "X966926",
      "X969031",
    ),
  ): ResponseEntity<List<CaseDto>> {
    val cases = caseService.getCases(crns, riskLevel)
    return ResponseEntity.ok(cases)
  }

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases/{crn}")
  fun getCase(@PathVariable crn: String): ResponseEntity<CaseDto> {
    val cases = caseService.getCase(crn)
    return ResponseEntity.ok(cases)
  }
}

enum class Status {
  TBC,
}
