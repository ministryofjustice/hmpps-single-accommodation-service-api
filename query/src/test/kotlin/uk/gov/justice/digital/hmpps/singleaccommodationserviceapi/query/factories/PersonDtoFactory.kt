package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.domain.Name
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Officer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOfficer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.FullPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.LimitedPersonDto
import java.time.LocalDate

fun buildFullPersonDto(
  crn: String,
  name: Name = buildName(),
  nomsNumber: String = "PRI1",
  pncNumber: String = "Some PNC Reference",
  dateOfBirth: LocalDate = LocalDate.of(2000, 12, 3),
  staff: Officer = buildOfficer(),
  gender: String = "Male",
  roshLevel: RiskLevel? = RiskLevel.VERY_HIGH,
  teamCode: String = "TEAM1",
  assignedTo: AssignedToDto = assignedTo(staff),
  limitedAccess: Boolean = false,
) = FullPersonDto(
  crn = crn,
  name = name.fullName,
  nomsNumber = nomsNumber,
  pncNumber = pncNumber,
  dateOfBirth = dateOfBirth,
  gender = gender,
  roshLevel = roshLevel,
  teamCode = teamCode,
  assignedTo = assignedTo,
  limitedAccess = limitedAccess,
)

fun buildLimitedPersonDto(
  crn: String,
  nomsNumber: String = "PRI1",
  staff: Officer = buildOfficer(),
  teamCode: String = "TEAM1",
  assignedTo: AssignedToDto = assignedTo(staff),
) = LimitedPersonDto(
  crn = crn,
  nomsNumber = nomsNumber,
  teamCode = teamCode,
  assignedTo = assignedTo,
)

private fun assignedTo(staff: Officer) = buildAssignedToDto(
  forename = staff.name.forename,
  surname = staff.name.surname,
  username = staff.username,
  staffCode = staff.code,
)
