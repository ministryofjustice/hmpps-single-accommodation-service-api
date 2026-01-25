package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock.MockData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer

@RestController
class EligibilityController(
  val eligibilityService: EligibilityService,
  private val mockedData: MockData?,
) {

  @PreAuthorize("hasRole('ROLE_PROBATION')")
  @GetMapping("/cases/{crn}/eligibility")
  fun getEligibility(@PathVariable crn: String): ResponseEntity<EligibilityDto> {
    val eligibility = eligibilityService.getEligibility(crn)
    return mockedData
      ?.let {
        val currentMock = mockedData.crns[crn]!!
        ResponseEntity.ok(
          EligibilityTransformer.toEligibilityDto(
            crn = crn,
            cas1 = eligibility.cas1,
            cas3 = currentMock.cas3Eligibility,
            cas2Hdc = currentMock.cas2HdcEligibility,
            cas2PrisonBail = currentMock.cas2PrisonBailEligibility,
            cas2CourtBail = currentMock.cas2CourtBailEligibility,
          ),
        )
      }
      ?: ResponseEntity.ok(eligibility)
  }
}
