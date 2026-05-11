package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Name
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Officer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOfficer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.ExcludedPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.FullPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.RestrictedPersonDto
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
)

fun buildRestrictedPersonDto(
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
) = RestrictedPersonDto(
  crn = crn,
  name = name.fullName,
  nomsNumber = nomsNumber,
  pncNumber = pncNumber,
  dateOfBirth = dateOfBirth,
  gender = gender,
  roshLevel = roshLevel,
  teamCode = teamCode,
  assignedTo = assignedTo,
)

fun buildExcludedPersonDto(
  crn: String,
  nomsNumber: String = "PRI1",
  staff: Officer = buildOfficer(),
  teamCode: String = "TEAM1",
  assignedTo: AssignedToDto = assignedTo(staff),
) = ExcludedPersonDto(
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
