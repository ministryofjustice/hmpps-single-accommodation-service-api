package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.toEligibilityDto

@RestController
class EligibilityController(
  val eligibilityService: EligibilityService,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases/{crn}/eligibility")
  fun getEligibility(@PathVariable crn: String): ResponseEntity<EligibilityDto> {
    val eligibility = eligibilityService.getEligibility(crn)
    return ResponseEntity.ok(
      toEligibilityDto(
        crn = crn,
        cas1 = eligibility.cas1,
        cas2Hdc = eligibility.cas2Hdc,
        cas2PrisonBail = eligibility.cas2PrisonBail,
        cas2CourtBail = eligibility.cas2CourtBail,
        cas3 = eligibility.cas3,
      ),
    )
  }
}
