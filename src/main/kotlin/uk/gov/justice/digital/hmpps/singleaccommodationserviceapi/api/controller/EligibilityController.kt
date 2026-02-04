package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService

@RestController
class EligibilityController(
  val eligibilityService: EligibilityService,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases/{crn}/eligibility")
  fun getEligibility(@PathVariable crn: String): ResponseEntity<EligibilityDto> = ResponseEntity.ok(eligibilityService.getEligibility(crn))
}
