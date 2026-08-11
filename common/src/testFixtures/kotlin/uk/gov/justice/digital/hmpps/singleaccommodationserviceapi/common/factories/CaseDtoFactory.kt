package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.StaffDetailsDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import java.time.LocalDate

fun buildCaseDto(
  crn: String,
  forename: String = "First",
  middleNames: String? = "Middle",
  surname: String = "Last",
  dateOfBirth: LocalDate? = LocalDate.of(2000, 12, 3),
  prisonNumber: String? = "PRI1",
  tierScore: String? = "A1",
  riskLevel: RiskLevel? = RiskLevel.VERY_HIGH,
  pncReference: String? = "Some PNC Reference",
  assignedTo: AssignedToDto? = buildAssignedToDto(),
  userAccess: UserAccess = UserAccess.FULL,
  limitedAccess: Boolean = false,
) = CaseDto(
  forename = forename,
  middleNames = middleNames,
  surname = surname,
  dateOfBirth = dateOfBirth,
  crn = crn,
  prisonNumber = prisonNumber,
  photoUrl = null,
  tierScore = tierScore,
  riskLevel = riskLevel,
  pncReference = pncReference,
  assignedTo = assignedTo,
  userAccess = userAccess,
  limitedAccess = limitedAccess,
)

fun buildAssignedToDto(
  forename: String = "First",
  surname: String = "Last",
  username: String? = "user1",
) = AssignedToDto(
  forename = forename,
  surname = surname,
  username = username,
)

fun buildStaffDetailDto(
  name: String,
  username: String? = "user1",
) = StaffDetailsDto(
  name = name,
  username = username,
)
