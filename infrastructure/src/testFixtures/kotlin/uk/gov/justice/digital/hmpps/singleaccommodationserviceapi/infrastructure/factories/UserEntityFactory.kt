package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import java.util.UUID
import kotlin.String

fun buildUserEntity(
  id: UUID = UUID.randomUUID(),
  username: String = "joe.bloggs12",
  authSource: AuthSource = AuthSource.DELIUS,
  forename: String = "Joe",
  middleNames: String? = null,
  surname: String = " Bloggs",
  email: String? = "joe.bloggs@gmail.com",
  telephoneNumber: String? = null,
  nomisStaffId: Long? = null,
  nomisAccountType: String? = null,
  nomisActiveCaseloadId: String? = null,
  isEnabled: Boolean = true,
  isActive: Boolean = true,
) = UserEntity(
  id = id,
  username = username,
  authSource = authSource,
  forename = forename,
  middleNames = middleNames,
  surname = surname,
  email = email,
  telephoneNumber = telephoneNumber,
  nomisStaffId = nomisStaffId,
  nomisAccountType = nomisAccountType,
  nomisActiveCaseloadId = nomisActiveCaseloadId,
  isEnabled = isEnabled,
  isActive = isActive,
)
