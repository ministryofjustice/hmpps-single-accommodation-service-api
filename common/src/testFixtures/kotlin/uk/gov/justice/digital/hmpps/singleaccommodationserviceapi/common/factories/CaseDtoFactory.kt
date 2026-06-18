package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.StaffDetailsDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import java.time.LocalDate

fun buildCaseDto(
  crn: String,
  name: String = "First Middle Last",
  dateOfBirth: LocalDate? = LocalDate.of(2000, 12, 3),
  prisonNumber: String? = "PRI1",
  tierScore: String? = "A1",
  riskLevel: RiskLevel? = RiskLevel.VERY_HIGH,
  pncReference: String? = "Some PNC Reference",
  assignedTo: AssignedToDto? = buildAssignedToDto(),
  currentAccommodation: AccommodationSummaryDto? = null,
  nextAccommodation: AccommodationSummaryDto? = null,
  status: Status? = null,
  actions: List<CaseAction> = emptyList(),
  userAccess: UserAccess = UserAccess.FULL,
  limitedAccess: Boolean = false,
) = CaseDto(
  name,
  dateOfBirth,
  crn,
  prisonNumber,
  photoUrl = null,
  tierScore,
  riskLevel,
  pncReference,
  assignedTo,
  currentAccommodation,
  nextAccommodation,
  status,
  actions,
  userAccess = userAccess,
  limitedAccess = limitedAccess,
)

fun buildAssignedToDto(
  forename: String = "First",
  surname: String = "Last",
  username: String? = "user1",
  staffCode: String? = "ABCD1234",
) = AssignedToDto(
  forename = forename,
  surname = surname,
  username = username,
  staffCode = staffCode,
)

fun buildStaffDetailDto(
  name: String,
  username: String? = "user1",
  staffCode: String? = "ABCD1234",
) = StaffDetailsDto(
  name = name,
  username = username,
  staffCode = staffCode,
)
