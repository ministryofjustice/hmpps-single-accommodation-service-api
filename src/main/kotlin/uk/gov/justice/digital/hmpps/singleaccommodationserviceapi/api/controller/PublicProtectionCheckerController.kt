package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PpcCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PpcCommandResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.PublicProtectionCheckerApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService

@RestController
class PublicProtectionCheckerController(
  private val publicProtectionCheckerApplicationService: PublicProtectionCheckerApplicationService,
  private val eligibilityService: EligibilityService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PostMapping("/public-protection-checker/{crn}")
  @ResponseStatus(HttpStatus.CREATED)
  fun create(
    @PathVariable crn: String,
    @RequestBody command: PpcCommand,
  ): ResponseEntity<PpcCommandResponse> {
    val entity = publicProtectionCheckerApplicationService.createPublicProtectionCheck(crn, command)
    val serviceResult = eligibilityService.getEligibility(crn, entity)
    val response = PpcCommandResponse(
      publicProtectionRequired = serviceResult.serviceStatus == ServiceStatusV2.PPC_COMPLETED_PUBLIC_PROTECTION_REQUIRED,
    )
    return ResponseEntity(response, HttpStatus.CREATED)
  }
}
