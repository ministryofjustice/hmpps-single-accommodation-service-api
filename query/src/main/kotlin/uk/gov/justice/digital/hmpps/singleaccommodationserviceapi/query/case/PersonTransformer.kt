package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PersonNamesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Name

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
      personNames = case.name.toPersonNamesDto(),
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

  private fun Name.toPersonNamesDto() = if (forename.isBlank() || surname.isBlank()) {
    null
  } else {
    PersonNamesDto(
      forename = forename,
      middleNames = middleName?.takeIf { it.isNotBlank() },
      surname = surname,
    )
  }
}
