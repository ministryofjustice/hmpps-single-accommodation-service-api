package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock.MockDataDto

@RestController
class EligibilityController(
  val eligibilityService: EligibilityService,
  private val mockedData: MockDataDto?,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases/{crn}/eligibility")
  fun getEligibility(@PathVariable crn: String): ResponseEntity<EligibilityDto> {
    val cas1Eligibility = eligibilityService.getEligibility(crn)
    val mockEligibility = mockedData?.crns?.get(crn)?.eligibility

    val eligibility = EligibilityDto(
      crn = cas1Eligibility.crn,
      cas1 = cas1Eligibility.cas1,
      caseStatus = mockEligibility?.caseStatus,
      caseActions = mockEligibility?.caseActions,
      cas2Hdc = mockEligibility?.cas2?.hdc,
      cas2PrisonBail = mockEligibility?.cas2?.prisonBail,
      cas2CourtBail = mockEligibility?.cas2?.courtBail,
      cas3 = mockEligibility?.cas3,
    )

    return ResponseEntity.ok(eligibility)
  }
}
