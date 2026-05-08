package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import java.time.LocalDate

fun buildCaseDto(
  crn: String,
  name: String = "First Middle Last",
  dateOfBirth: LocalDate? = LocalDate.of(2000, 12, 3),
  prisonNumber: String? = "PRI1",
  tierScore: TierScore? = TierScore.A1,
  riskLevel: RiskLevel? = RiskLevel.VERY_HIGH,
  pncReference: String? = "Some PNC Reference",
  assignedTo: AssignedToDto? = buildAssignedToDto(),
  currentAccommodation: AccommodationDetail? = null,
  nextAccommodation: AccommodationDetail? = null,
  status: Status? = null,
  actions: List<String> = emptyList(),
  caseAccess: CaseAccess = CaseAccess.FULL,
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
  caseAccess = caseAccess,
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
