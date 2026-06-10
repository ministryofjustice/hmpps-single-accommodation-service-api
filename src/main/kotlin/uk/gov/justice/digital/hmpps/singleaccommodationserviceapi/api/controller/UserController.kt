package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.UserQueryService

@RestController
class UserController(private val userQueryService: UserQueryService) {
  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/user/teams")
  fun getTeams() = ApiResponseDto(userQueryService.getTeams())
}
