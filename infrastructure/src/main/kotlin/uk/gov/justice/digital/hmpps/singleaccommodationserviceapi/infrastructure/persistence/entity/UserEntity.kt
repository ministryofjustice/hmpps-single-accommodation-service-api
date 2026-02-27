package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "sas_user")
data class UserEntity(
  @Id
  val id: UUID,
  val username: String,
  @Enumerated(EnumType.STRING)
  val authSource: AuthSource,
  var name: String,
  var email: String?,
  var telephoneNumber: String?,
  var deliusStaffCode: String?,
  var nomisStaffId: Long?,
  var nomisAccountType: String?,
  var nomisActiveCaseloadId: String?,
  var isEnabled: Boolean,
  var isActive: Boolean,
)

enum class AuthSource(val value: String) {
  DELIUS("delius"),
  NOMIS("nomis"),
  ;

  companion object {
    fun fromString(authSource: String): AuthSource = AuthSource.entries.first { it.value == authSource }
  }
}
