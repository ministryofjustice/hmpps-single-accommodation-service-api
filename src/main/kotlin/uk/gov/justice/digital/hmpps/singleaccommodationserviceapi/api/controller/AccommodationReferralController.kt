package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.constants.Roles
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralQueryService

@RestController
@PreAuthorize("hasRole('${Roles.ROLE_PROBATION}')")
class AccommodationReferralController(
  private val accommodationReferralQueryService: AccommodationReferralQueryService,
) {

  @Operation(
    description = """Retrieve application history by CRN. Role required is ${Roles.ROLE_PROBATION}""",
    security = [SecurityRequirement(name = "role-probation")],
  )
  @GetMapping("/cases/{crn}/applications")
  fun getApplicationHistory(@PathVariable crn: String): ResponseEntity<List<AccommodationReferralDto>> {
    val referralHistory = accommodationReferralQueryService.getReferralHistory(crn)
    return ResponseEntity.ok(referralHistory)
  }
}
