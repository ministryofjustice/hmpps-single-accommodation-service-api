package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.Borough
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.Ldu
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.Manager
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.Name
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.Profile
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.Team
import java.time.LocalDate

fun buildCaseSummary(
  crn: String = "XX12345X",
  nomsId: String? = "YY09876Y",
  pnc: String? = "Some PNC Reference",
  name: Name = buildName(),
  dateOfBirth: LocalDate = LocalDate.now().minusYears(25),
  gender: String? = "F",
  profile: Profile? = buildProfile(),
  manager: Manager = buildManager(),
  currentExclusion: Boolean = false,
  currentRestriction: Boolean = false,
) = CaseSummary(
  crn = crn,
  nomsId = nomsId,
  pnc = pnc,
  name = name,
  dateOfBirth = dateOfBirth,
  gender = gender,
  profile = profile,
  manager = manager,
  currentExclusion = currentExclusion,
  currentRestriction = currentRestriction,
)

fun buildName(
  forename: String = "Fore",
  surname: String = "Sur",
  middleNames: List<String> = listOf("Middle", "Name"),
) = Name(
  forename = forename,
  surname = surname,
  middleNames = middleNames,
)

fun buildProfile() = Profile(
  ethnicity = "TODO()",
  genderIdentity = "TODO()",
  nationality = "TODO()",
  religion = "TODO()",
)

fun buildManager(team: Team = buildTeam()) = Manager(
  team = team,
)

fun buildTeam() = Team(
  code = "TODO(code)",
  name = "Team 1",
  ldu = Ldu(
    code = "TODO(code)",
    name = "TODO(name)",
  ),
  borough = Borough(
    code = "TODO(code)",
    description = "TODO(description)",
  ),
  startDate = LocalDate.now().minusYears(1),
  endDate = LocalDate.now().plusYears(1),
)
