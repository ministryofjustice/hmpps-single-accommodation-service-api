package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case

object PersonTransformer {
  fun toPersonDto(
    case: Case,
  ): PersonDto = if (case.userExcluded || case.userRestricted) {
    LimitedPersonDto(
      crn = case.crn,
      nomsNumber = case.nomsNumber,
      teamCode = case.team.code,
      assignedTo = case.getAssignedTo(),
    )
  } else {
    FullPersonDto(
      crn = case.crn,
      name = case.name.fullName,
      nomsNumber = case.nomsNumber,
      pncNumber = case.pncNumber,
      dateOfBirth = case.dateOfBirth,
      gender = case.gender,
      riskLevel = case.getRiskLevel(),
      teamCode = case.team.code,
      assignedTo = case.getAssignedTo(),
      limitedAccess = case.limitedAccess,
    )
  }

  fun Case.getRiskLevel() = roshLevel?.code?.let { RiskLevel.findByCode(it) }
  fun Case.getAssignedTo() = AssignedToDto(
    forename = staff.name.forename,
    surname = staff.name.surname,
    username = staff.username,
  )
}
