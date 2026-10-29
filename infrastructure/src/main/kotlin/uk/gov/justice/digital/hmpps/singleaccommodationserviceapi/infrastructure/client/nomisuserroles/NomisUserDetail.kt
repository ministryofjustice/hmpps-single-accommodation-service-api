package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles

import java.time.LocalDateTime

data class NomisUserDetail(
  val username: String,
  val staffId: Long,
  val firstName: String,
  val lastName: String,
  val activeCaseloadId: String? = null,
  val accountStatus: String? = null,
  val accountType: String,
  val primaryEmail: String? = null,
  val dpsRoleCodes: List<String>,
  val administratorOfUserGroups: List<UserGroupDetail>,
  val accountNonLocked: Boolean? = null,
  val credentialsNonExpired: Boolean? = null,
  val enabled: Boolean,
  val admin: Boolean? = null,
  val active: Boolean,
  val staffStatus: String? = null,
  val lastLogonDate: LocalDateTime? = null,
)

data class UserGroupDetail(
  val id: String,
  val name: String,
)
