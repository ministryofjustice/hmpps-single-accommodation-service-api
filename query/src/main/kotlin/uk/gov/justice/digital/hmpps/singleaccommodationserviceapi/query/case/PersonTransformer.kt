package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Name

object PersonTransformer {
  fun toPersonDto(
    case: Case,
  ) = PersonDto(
    crn = case.crn,
    name = toFullName(case.name),
    nomsNumber = case.nomsNumber,
    pncNumber = case.pncNumber,
    dateOfBirth = case.dateOfBirth,
    staff = case.staff,
    gender = case.gender,
    roshLevelCode = case.roshLevel?.code,
    expectedReleaseDate = case.expectedReleaseDate,
  )
}

fun toFullName(name: Name) = listOfNotNull(
  name.forename,
  name.middleName?.takeIf { it.isNotBlank() },
  name.surname,
).joinToString(" ")
