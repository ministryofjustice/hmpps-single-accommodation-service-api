package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import java.util.UUID

@Entity
@Table(name = "sas_user")
data class UserEntity(
  @Id
  val id: UUID,
  val username: String,
  @Enumerated(EnumType.STRING)
  val authSource: AuthSource,
  var forename: String,
  var middleNames: String?,
  var surname: String,
  var email: String?,
  var telephoneNumber: String?,
  var deliusStaffCode: String?,
  var nomisStaffId: Long?,
  var nomisAccountType: String?,
  var nomisActiveCaseloadId: String?,
  var isEnabled: Boolean?, // only on NomisUserDetail
  var isActive: Boolean,
) {
  fun displayName() = "$forename $surname"
}

fun UserEntity.toAssignedToDto() = AssignedToDto(
  forename = forename,
  surname = surname,
  username = username,
  staffCode = deliusStaffCode,
)

enum class AuthSource(val source: String) {
  NOMIS("nomis"),
  DELIUS("delius"),
  NONE("none"),
}
