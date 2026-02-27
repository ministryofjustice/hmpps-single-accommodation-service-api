package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles.NomisUserDetail
import kotlin.String

fun buildNomisUserDetail(
  username: String = "nomisuser@justice.gov.uk",
  staffId: Long = 1L,
  firstName: String = "Test",
  lastName: String = "User",
  activeCaseloadId: String? = "MDI",
  accountStatus: String? = "OPEN",
  accountType: String = "GENERAL",
  primaryEmail: String? = "test.user@justice.gov.uk",
  dpsRoleCodes: List<String> = emptyList(),
  accountNonLocked: Boolean? = true,
  credentialsNonExpired: Boolean? = true,
  enabled: Boolean = true,
  admin: Boolean? = false,
  active: Boolean = true,
  staffStatus: String? = "ACTIVE",
) = NomisUserDetail(
  username = username,
  staffId = staffId,
  firstName = firstName,
  lastName = lastName,
  activeCaseloadId = activeCaseloadId,
  accountStatus = accountStatus,
  accountType = accountType,
  primaryEmail = primaryEmail,
  dpsRoleCodes = dpsRoleCodes,
  accountNonLocked = accountNonLocked,
  credentialsNonExpired = credentialsNonExpired,
  enabled = enabled,
  admin = admin,
  active = active,
  staffStatus = staffStatus,
)
