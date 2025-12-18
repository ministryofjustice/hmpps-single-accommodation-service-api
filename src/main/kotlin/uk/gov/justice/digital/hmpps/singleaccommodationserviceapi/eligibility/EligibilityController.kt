package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class EligibilityController(
  val eligibilityService: EligibilityService,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases/{crn}/eligibility")
  fun getEligibility(@PathVariable crn: String): ResponseEntity<EligibilityDto> {
    val eligibility = eligibilityService.getEligibility(crn)
    return ResponseEntity.ok(eligibility)
  }
}
