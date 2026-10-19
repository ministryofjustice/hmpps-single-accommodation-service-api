package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user

interface User {
  val username: String
}

data class DeliusUserDto(override val username: String, val teams: List<TeamDto>, val probationAreaCode: String) : User

data class NomisUserDto(override val username: String) : User

data class TeamDto(
  val name: String,
  val code: String,
)
