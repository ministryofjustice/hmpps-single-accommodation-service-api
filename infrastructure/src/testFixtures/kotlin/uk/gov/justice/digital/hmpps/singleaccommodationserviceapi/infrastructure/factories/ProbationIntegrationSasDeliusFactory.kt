package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Name
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Officer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.RoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Team
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.TestData
import java.time.LocalDate

@TestData
fun buildCase(
  crn: String = "XX12345X",
  nomsNumber: String? = "YY09876Y",
  pncNumber: String? = "Some PNC Reference",
  name: Name = buildName(),
  dateOfBirth: LocalDate = LocalDate.of(2000, 12, 3),
  gender: String = "Male",
  userExcluded: Boolean = false,
  userRestricted: Boolean = false,
  exclusionMessage: String? = null,
  restrictionMessage: String? = null,
  staff: Officer = buildOfficer(),
  roshLevel: RoshLevel = buildRoshLevel(),
  team: Team = buildCaseTeam(),
) = Case(
  crn = crn,
  name = name,
  dateOfBirth = dateOfBirth,
  gender = gender,
  nomsNumber = nomsNumber,
  pncNumber = pncNumber,
  staff = staff,
  team = team,
  roshLevel = roshLevel,
  userExcluded = userExcluded,
  userRestricted = userRestricted,
  exclusionMessage = exclusionMessage,
  restrictionMessage = restrictionMessage,
)

fun buildName(
  forename: String = "First",
  surname: String = "Last",
  middleName: String = "Middle",
) = Name(
  forename = forename,
  surname = surname,
  middleName = middleName,
)

fun buildOfficer(
  name: Name = buildName(),
  username: String = "user1",
  code: String = "ABCD1234",
) = Officer(
  name = name,
  username = username,
  code = code,
)

fun buildCaseTeam(
  code: String = "ABC123",
  description: String = "A description",
) = Team(
  code = code,
  description = description,
)

fun buildRoshLevel(
  code: String = "RVHR",
  description: String = "Very high risk",
) = RoshLevel(
  code = code,
  description = description,
)
