package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.Borough
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummaryName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.Ldu
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.Manager
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.PersonName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.Profile
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.StaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.Team
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.TestData
import java.time.LocalDate

@TestData
fun buildCaseSummary(
  crn: String = "XX12345X",
  nomsId: String? = "YY09876Y",
  pnc: String? = "Some PNC Reference",
  name: CaseSummaryName = buildCaseSummaryName(),
  dateOfBirth: LocalDate = LocalDate.of(2000, 12, 3),
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

fun buildCaseSummaryName(
  forename: String = "Fore",
  surname: String = "Sur",
  middleNames: List<String> = listOf("Middle", "Name"),
) = CaseSummaryName(
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

fun buildTeam(code: String = "12345", name: String = "Team 1") = Team(
  code,
  name = name,
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

fun buildStaffDetail(
  username: String = "test.user",
  email: String = "test.user@justice.gov.uk",
  name: PersonName = buildPersonName(),
  teams: List<Team> = listOf(buildTeam()),
) = StaffDetail(
  email = email,
  telephoneNumber = "07665111456",
  teams = teams,
  username = username,
  name = name,
  code = "code",
  active = true,
)

fun buildPersonName(
  forename: String = "Test",
  surname: String = "User",
) = PersonName(forename, surname)
