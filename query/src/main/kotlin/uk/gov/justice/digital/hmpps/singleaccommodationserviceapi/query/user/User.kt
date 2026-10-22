package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "userType",
)
@JsonSubTypes(
  JsonSubTypes.Type(value = DeliusUserDto::class, name = "DELIUS"),
  JsonSubTypes.Type(value = NomisUserDto::class, name = "NOMIS"),
)
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed interface UserDto {
  val username: String
}

data class DeliusUserDto(
  override val username: String,
  val teams: List<TeamDto>,
  val probationAreaCode: String,
) : UserDto {
  val userType: UserType
    get() = UserType.DELIUS
}

data class NomisUserDto(
  override val username: String,
  val prisonCode: String?,
) : UserDto {
  val userType: UserType
    get() = UserType.NOMIS
}

data class TeamDto(
  val name: String,
  val code: String,
)

enum class UserType { DELIUS, NOMIS }
