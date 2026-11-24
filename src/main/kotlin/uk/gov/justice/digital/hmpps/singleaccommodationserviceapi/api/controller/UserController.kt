package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpdateUserPreferencesCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.UserPreferenceApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.UserPreferencesQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.UserQueryService

@RestController
class UserController(
  private val userService: UserService,
  private val userQueryService: UserQueryService,
  private val userPreferencesQueryService: UserPreferencesQueryService,
  private val userPreferenceApplicationService: UserPreferenceApplicationService,
) {
  @Deprecated("Use /users/me")
  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/user/teams")
  fun getTeams() = ApiResponseDto(userQueryService.getTeams())

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/users/me")
  fun getUserData() = ApiResponseDto(userQueryService.getUserData())

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/users/me/team-members")
  fun getTeamMembers() = userQueryService.getUserTeamMembers()

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/users/me/preferences")
  fun getUserPreferences() = ApiResponseDto(userPreferencesQueryService.getUserPreferences())

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PatchMapping("/users/me/preferences")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateUserPreferences(@RequestBody command: UpdateUserPreferencesCommand): ResponseEntity<Void> {
    val user = userService.authorizeAndRetrieveUser()
    userPreferenceApplicationService.updatePreferences(user.id, command)
    return ResponseEntity(HttpStatus.NO_CONTENT)
  }
}
