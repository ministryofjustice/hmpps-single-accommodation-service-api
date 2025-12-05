package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.Referral
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.constants.Roles.ROLE_SAS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.service.referralhistory.ReferralHistoryService

@Service
@RestController
@RequestMapping("/sas/referral-history")
@PreAuthorize("hasRole('$ROLE_SAS_USER')")
class ReferralHistoryController(
  private val referralHistoryService: ReferralHistoryService,
) {

  @Operation(
    description = """Retrieve referral history by CRN. Role required is $ROLE_SAS_USER""",
    security = [SecurityRequirement(name = "role-sas-user")],
  )
  @GetMapping("/{crn}")
  fun getReferralHistory(@PathVariable crn: String): ResponseEntity<List<Referral>> {
    val referralHistory = referralHistoryService.getReferralHistory(crn)
    return ResponseEntity.ok(referralHistory)
  }
}
