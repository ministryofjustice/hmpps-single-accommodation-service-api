package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import java.util.UUID
import kotlin.String

fun buildUserEntity(
  id: UUID = UUID.randomUUID(),
  username: String = "joe.bloggs12",
  authSource: AuthSource = AuthSource.DELIUS,
  name: String = "Joe Bloggs",
  email: String? = "joe.bloggs@gmail.com",
  telephoneNumber: String? = null,
  deliusStaffCode: String? = null,
  nomisStaffId: Long? = null,
  nomisAccountType: String? = null,
  nomisActiveCaseloadId: String? = null,
  isEnabled: Boolean = true,
  isActive: Boolean = true,
) = UserEntity(
  id = id,
  username = username,
  authSource = authSource,
  name = name,
  email = email,
  telephoneNumber = telephoneNumber,
  deliusStaffCode = deliusStaffCode,
  nomisStaffId = nomisStaffId,
  nomisAccountType = nomisAccountType,
  nomisActiveCaseloadId = nomisActiveCaseloadId,
  isEnabled = isEnabled,
  isActive = isActive,
)
