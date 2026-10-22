package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class UserDto(
  val userType: UserType,
  val username: String,
  val teams: List<TeamDto> = emptyList(),
  val probationAreaCode: String? = null,
  val prisonCode: String? = null,
)

data class TeamDto(
  val name: String,
  val code: String,
)

enum class UserType { DELIUS, NOMIS }
