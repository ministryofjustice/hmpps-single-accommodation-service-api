package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummariesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PeopleType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.filterCases
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.obfuscateIfLimitedAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.obfuscateLimitedAccessCases
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.sortCases
import java.time.LocalDate

class CaseListSortingTest {
  private val today: LocalDate = LocalDate.of(2026, 10, 1)

  private fun case(
    crn: String,
    status: CaseAccommodationStatus?,
    statusDate: LocalDate? = null,
    forename: String = "First",
    surname: String = "Last",
    withSummaries: Boolean = true,
  ): CaseDto = buildCaseDto(
    crn = crn,
    forename = forename,
    surname = surname,
    accommodationSummaries = if (withSummaries) {
      AccommodationSummariesDto(caseAccommodationStatus = status, caseAccommodationStatusDate = statusDate)
    } else {
      null
    },
  )

  @Nested
  inner class Filtered {
    private val currentUser = "user1"
    private val otherUser = "user2"

    private fun full(
      crn: String,
      forename: String = "First",
      surname: String = "Last",
      prisonNumber: String? = null,
      riskLevel: RiskLevel? = null,
      teamCode: String? = "TeamA",
      assignedToUsername: String? = currentUser,
      status: CaseAccommodationStatus? = null,
    ): CaseDto = buildCaseDto(
      crn = crn,
      forename = forename,
      middleNames = null,
      surname = surname,
      prisonNumber = prisonNumber,
      riskLevel = riskLevel,
      teamCode = teamCode,
      assignedTo = buildAssignedToDto(username = assignedToUsername),
      accommodationSummaries = status?.let { AccommodationSummariesDto(caseAccommodationStatus = it) },
    )

    private fun limited(crn: String, teamCode: String? = null, assignedToUsername: String? = currentUser) = CaseDto(
      crn = crn,
      userAccess = UserAccess.LIMITED,
      limitedAccess = true,
      teamCode = teamCode,
      assignedTo = assignedToUsername?.let { buildAssignedToDto(username = it) },
    )

    private fun List<CaseDto>.applyFilters(
      searchTerm: String? = null,
      riskLevel: RiskLevel? = null,
      teamCode: String? = null,
      peopleType: PeopleType? = null,
      username: String? = currentUser,
    ) = filterCases(
      searchTerm = searchTerm,
      riskLevel = riskLevel,
      teamCode = teamCode,
      username = username,
      peopleType = peopleType,
    ).map { it.crn }

    @Test
    fun `returns only cases assigned to the current user when no filters provided`() {
      val cases = listOf(
        full("MINE1"),
        full("MINE2", assignedToUsername = "USER1"),
        full("OTHER", assignedToUsername = otherUser),
        full("UNASSIGNED", assignedToUsername = null),
      )

      assertThat(cases.applyFilters()).containsExactlyInAnyOrder("MINE1", "MINE2")
    }

    @Test
    fun `returns empty list for empty input`() {
      assertThat(emptyList<CaseDto>().applyFilters()).isEmpty()
    }

    @Test
    fun `does not filter by assignee when no username is provided`() {
      val cases = listOf(
        full("MINE"),
        full("OTHER", assignedToUsername = otherUser),
        full("UNASSIGNED", assignedToUsername = null),
      )

      assertThat(cases.applyFilters(username = null)).containsExactlyInAnyOrder("MINE", "OTHER", "UNASSIGNED")
    }

    @Test
    fun `matches the username ignoring case`() {
      val cases = listOf(full("MINE", assignedToUsername = "User1"), full("OTHER", assignedToUsername = otherUser))

      assertThat(cases.applyFilters(username = "USER1")).containsExactly("MINE")
    }

    @Nested
    inner class ByTeam {
      private val cases = listOf(
        full("A_MINE", teamCode = "TeamA"),
        full("A_OTHER_USER", teamCode = "TeamA", assignedToUsername = otherUser),
        full("B_MINE", teamCode = "TeamB"),
        full("A_NO_TEAM", teamCode = null),
        limited("A_LIMITED", teamCode = "TeamA"),
      )

      @Test
      fun `returns cases in the team, including limited access ones, regardless of assigned user, ignoring case`() {
        assertThat(cases.applyFilters(teamCode = "teama")).containsExactlyInAnyOrder(
          "A_MINE",
          "A_OTHER_USER",
          "A_LIMITED",
        )
      }

      @Test
      fun `returns nothing for an unknown team`() {
        assertThat(cases.applyFilters(teamCode = "TeamZ")).isEmpty()
      }

      @Test
      fun `falls back to the current user when team code is blank`() {
        assertThat(cases.applyFilters(teamCode = " "))
          .containsExactlyInAnyOrder("A_MINE", "B_MINE", "A_NO_TEAM", "A_LIMITED")
      }
    }

    @Nested
    inner class BySearchTerm {
      private val cases = listOf(
        full("X100001", forename = "First", surname = "MultiCaseSurname", prisonNumber = "PRI_1"),
        full("X100002", forename = "QQQQQ", surname = "Last", prisonNumber = "PRI_2"),
        full("X100003", forename = "First", surname = "Last", prisonNumber = null),
        limited("X100004"),
      )

      @Test
      fun `matches a full CRN ignoring case, including limited access cases`() {
        assertThat(cases.applyFilters(searchTerm = "x100001")).containsExactly("X100001")
        assertThat(cases.applyFilters(searchTerm = "x100004")).containsExactly("X100004")
      }

      @Test
      fun `does not match a partial CRN`() {
        assertThat(cases.applyFilters(searchTerm = "X1000")).isEmpty()
      }

      @Test
      fun `matches a full prison number ignoring case but not a partial one`() {
        assertThat(cases.applyFilters(searchTerm = "pri_2")).containsExactly("X100002")
        assertThat(cases.applyFilters(searchTerm = "pri_")).isEmpty()
      }

      @Test
      fun `matches partial names across forename and surname ignoring case`() {
        assertThat(cases.applyFilters(searchTerm = "qqqqq")).containsExactly("X100002")
        assertThat(cases.applyFilters(searchTerm = "multi")).containsExactly("X100001")
        assertThat(cases.applyFilters(searchTerm = "rSt la")).containsExactly("X100003")
        assertThat(cases.applyFilters(searchTerm = "lAsT fIrSt")).containsExactly("X100003")
        assertThat(cases.applyFilters(searchTerm = " Last   First ")).containsExactly("X100003")
      }

      @Test
      fun `does not match the text null when name fields are missing`() {
        val noNames = CaseDto(crn = "X100005", userAccess = UserAccess.FULL, limitedAccess = false, assignedTo = buildAssignedToDto(username = currentUser))
        val forenameOnly = CaseDto(crn = "X100006", forename = "Zed", userAccess = UserAccess.FULL, limitedAccess = false, assignedTo = buildAssignedToDto(username = currentUser))
        val cases = listOf(noNames, forenameOnly)

        assertThat(cases.applyFilters(searchTerm = "n")).isEmpty()
        assertThat(cases.applyFilters(searchTerm = "null")).isEmpty()
        assertThat(cases.applyFilters(searchTerm = "zed")).containsExactly("X100006")
      }

      @Test
      fun `does not return limited access cases for a name search`() {
        assertThat(cases.applyFilters(searchTerm = "First")).doesNotContain("X100004")
      }

      @Test
      fun `returns no cases when nothing matches`() {
        assertThat(cases.applyFilters(searchTerm = "xxxxx")).isEmpty()
      }

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = ["  "])
      fun `does not filter when search term is null or blank`(searchTerm: String?) {
        assertThat(cases.applyFilters(searchTerm = searchTerm)).hasSize(cases.size)
      }
    }

    @Nested
    inner class ByRiskLevel {
      private val cases = listOf(
        full("NONE", riskLevel = null),
        full("LOW", riskLevel = RiskLevel.LOW),
        full("MEDIUM", riskLevel = RiskLevel.MEDIUM),
        full("VERY_HIGH", riskLevel = RiskLevel.VERY_HIGH),
        limited("LIMITED"),
      )

      @ParameterizedTest
      @EnumSource(RiskLevel::class, names = ["LOW", "MEDIUM", "VERY_HIGH"])
      fun `returns only full access cases with the requested risk level`(riskLevel: RiskLevel) {
        assertThat(cases.applyFilters(riskLevel = riskLevel)).containsExactly(riskLevel.name)
      }

      @Test
      fun `returns nothing when no case has the requested risk level`() {
        assertThat(cases.applyFilters(riskLevel = RiskLevel.HIGH)).isEmpty()
      }

      @Test
      fun `does not filter when risk level is null`() {
        assertThat(cases.applyFilters(riskLevel = null)).hasSize(cases.size)
      }
    }

    @Nested
    inner class ByPeopleType {
      private val settled = full("SETTLED", status = CaseAccommodationStatus.SETTLED)
      private val transient = full("TRANSIENT", status = CaseAccommodationStatus.TRANSIENT)
      private val nfa = full("NFA", status = CaseAccommodationStatus.NO_FIXED_ABODE)
      private val risk = full("RISK", status = CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE)
      private val noStatus = full("NONE", status = null)
      private val cases = listOf(settled, transient, nfa, risk, noStatus)

      @Test
      fun `returns all cases when people type is null`() {
        assertThat(cases.applyFilters(peopleType = null)).hasSize(cases.size)
      }

      @Test
      fun `HOUSED returns only settled cases`() {
        assertThat(cases.applyFilters(peopleType = PeopleType.HOUSED)).containsExactly("SETTLED")
      }

      @Test
      fun `NFA_RISK returns every case that is not settled, including those without a status`() {
        assertThat(cases.applyFilters(peopleType = PeopleType.NFA_RISK))
          .containsExactlyInAnyOrder("TRANSIENT", "NFA", "RISK", "NONE")
      }
    }

    @Test
    fun `combines all filters`() {
      val cases = listOf(
        full("MATCH", forename = "Alice", riskLevel = RiskLevel.HIGH, teamCode = "TeamA", status = CaseAccommodationStatus.SETTLED, assignedToUsername = otherUser),
        full("WRONG_RISK", forename = "Alice", riskLevel = RiskLevel.LOW, teamCode = "TeamA", status = CaseAccommodationStatus.SETTLED),
        full("WRONG_TEAM", forename = "Alice", riskLevel = RiskLevel.HIGH, teamCode = "TeamB", status = CaseAccommodationStatus.SETTLED),
        full("WRONG_NAME", forename = "Bob", riskLevel = RiskLevel.HIGH, teamCode = "TeamA", status = CaseAccommodationStatus.SETTLED),
        full("WRONG_TYPE", forename = "Alice", riskLevel = RiskLevel.HIGH, teamCode = "TeamA", status = CaseAccommodationStatus.TRANSIENT),
      )

      val result = cases.applyFilters(
        searchTerm = "alice",
        riskLevel = RiskLevel.HIGH,
        teamCode = "TeamA",
        peopleType = PeopleType.HOUSED,
      )

      assertThat(result).containsExactly("MATCH")
    }
  }

  @Nested
  inner class SortCases {
    private fun List<CaseDto>.crns() = map { it.crn }

    @Test
    fun `returns empty list for empty input`() {
      assertThat(emptyList<CaseDto>().sortCases(today)).isEmpty()
    }

    @Test
    fun `orders by status sort order with missing status first`() {
      val cases = listOf(
        case("C_SETTLED", CaseAccommodationStatus.SETTLED),
        case("C_TRANSIENT", CaseAccommodationStatus.TRANSIENT),
        case("C_NFA", CaseAccommodationStatus.NO_FIXED_ABODE),
        case("C_RISK", CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE),
        case("C_NONE", null, withSummaries = false),
      )

      assertThat(cases.sortCases(today).crns())
        .containsExactly("C_NONE", "C_RISK", "C_NFA", "C_TRANSIENT", "C_SETTLED")
    }

    @Test
    fun `sorts RISK_OF_NO_FIXED_ABODE by soonest status date`() {
      val cases = listOf(
        case("LATER", CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE, today.plusDays(5)),
        case("SOON", CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE, today.plusDays(1)),
      )

      assertThat(cases.sortCases(today).crns()).containsExactly("SOON", "LATER")
    }

    @Test
    fun `sorts NO_FIXED_ABODE by oldest status date`() {
      val cases = listOf(
        case("NEWER", CaseAccommodationStatus.NO_FIXED_ABODE, today.minusDays(1)),
        case("OLDER", CaseAccommodationStatus.NO_FIXED_ABODE, today.minusDays(5)),
      )

      assertThat(cases.sortCases(today).crns()).containsExactly("OLDER", "NEWER")
    }

    @Test
    fun `for TRANSIENT and SETTLED places missing date first, then future dates, then today and past dates`() {
      CaseAccommodationStatus.entries
        .filter { it == CaseAccommodationStatus.TRANSIENT || it == CaseAccommodationStatus.SETTLED }
        .forEach { status ->
          val cases = listOf(
            case("PAST", status, today.minusDays(1)),
            case("TODAY", status, today),
            case("FUTURE_FAR", status, today.plusDays(3)),
            case("FUTURE_NEAR", status, today.plusDays(1)),
            case("NO_DATE", status, null),
            case("PAST_OLD", status, today.minusDays(4)),
          )

          assertThat(cases.sortCases(today).crns())
            .containsExactly("NO_DATE", "FUTURE_NEAR", "FUTURE_FAR", "PAST_OLD", "PAST", "TODAY")
        }
    }

    @Test
    fun `uses today parameter to decide whether a date is in the future`() {
      val statusDate = LocalDate.of(2026, 10, 10)
      val cases = listOf(
        case("B", CaseAccommodationStatus.SETTLED, statusDate),
        case("A", CaseAccommodationStatus.SETTLED, LocalDate.of(2026, 10, 5)),
      )

      // statusDate and 5th Oct both in the future relative to 1st Oct -> oldest first
      assertThat(cases.sortCases(today).crns()).containsExactly("A", "B")
      // relative to 7th Oct, 5th is past and 10th is future -> future first
      assertThat(cases.sortCases(LocalDate.of(2026, 10, 7)).crns()).containsExactly("B", "A")
    }

    @Test
    fun `breaks ties by surname, forename ignoring case, then crn`() {
      val status = CaseAccommodationStatus.NO_FIXED_ABODE
      val date = today.minusDays(1)
      val cases = listOf(
        case("CRN5", status, date, forename = "zulu", surname = "Beta"),
        case("CRN3", status, date, forename = "Beta", surname = "beta"),
        case("CRN4", status, date, forename = "Zulu", surname = "Beta"),
        case("CRN2", status, date, forename = "Zulu", surname = "alpha"),
      )

      assertThat(cases.sortCases(today).crns()).containsExactly("CRN2", "CRN3", "CRN4", "CRN5")
    }
  }

  @Nested
  inner class Obfuscate {
    private fun caseWithAccess(crn: String, userAccess: UserAccess) = buildCaseDto(
      crn = crn,
      userAccess = userAccess,
      assignedTo = buildAssignedToDto(username = "user1"),
      teamCode = "TeamA",
    )

    @Test
    fun `removes assignee and team code from limited access cases only`() {
      val full = caseWithAccess("FULL1", UserAccess.FULL)
      val limited = caseWithAccess("LIMITED1", UserAccess.LIMITED)

      val result = listOf(full, limited).obfuscateLimitedAccessCases()

      assertThat(result[0]).isEqualTo(full)
      assertThat(result[1].assignedTo).isNull()
      assertThat(result[1].teamCode).isNull()
      assertThat(result[1].crn).isEqualTo("LIMITED1")
    }

    @Test
    fun `does not modify the original cases`() {
      val limited = caseWithAccess("LIMITED1", UserAccess.LIMITED)

      listOf(limited).obfuscateLimitedAccessCases()

      assertThat(limited.assignedTo).isNotNull()
      assertThat(limited.teamCode).isEqualTo("TeamA")
    }

    @Test
    fun `also removes assignee and team code when access is unknown`() {
      val result = listOf(caseWithAccess("UNKNOWN1", UserAccess.UNKNOWN)).obfuscateLimitedAccessCases().single()

      assertThat(result.assignedTo).isNull()
      assertThat(result.teamCode).isNull()
    }

    @Test
    fun `obfuscates a single limited case and leaves a full access case untouched`() {
      val full = caseWithAccess("FULL1", UserAccess.FULL)

      assertThat(full.obfuscateIfLimitedAccess()).isEqualTo(full)
      assertThat(caseWithAccess("LIMITED1", UserAccess.LIMITED).obfuscateIfLimitedAccess().assignedTo).isNull()
    }
  }
}
