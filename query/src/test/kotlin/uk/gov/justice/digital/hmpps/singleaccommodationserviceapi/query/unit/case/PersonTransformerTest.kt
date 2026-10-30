package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.NullSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseTeam
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOfficer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.FullPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.LimitedPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto
import java.time.LocalDate

class PersonTransformerTest {

  @Nested
  inner class ToPersonDto {

    @Test
    fun `should transform unrestricted case to FullPersonDto`() {
      val case = buildCase(
        crn = "X12345",
        nomsNumber = "A1234BC",
        pncNumber = "PNC123",
        dateOfBirth = LocalDate.of(1990, 1, 2),
        gender = "Female",
        name = buildName(forename = "Jane", middleName = "Alex", surname = "Doe"),
        staff = buildOfficer(
          name = buildName(forename = "Sam", surname = "Jones", middleName = ""),
          username = "sjones",
        ),
        team = buildCaseTeam(code = "TEAM1"),
        roshLevel = buildRoshLevel(code = RiskLevel.HIGH.code),
        limitedAccess = true,
      )

      val result = toPersonDto(case, UserAccess.FULL)

      assertThat(result).isInstanceOf(FullPersonDto::class.java)
      val fullResult = result as FullPersonDto
      assertThat(fullResult).isEqualTo(
        FullPersonDto(
          crn = "X12345",
          name = "Jane Alex Doe",
          forename = "Jane",
          middleNames = "Alex",
          surname = "Doe",
          nomsNumber = "A1234BC",
          pncNumber = "PNC123",
          dateOfBirth = LocalDate.of(1990, 1, 2),
          gender = "Female",
          riskLevel = RiskLevel.HIGH,
          teamCode = "TEAM1",
          assignedTo = AssignedToDto(
            forename = "Sam",
            surname = "Jones",
            username = "sjones",
          ),
          limitedAccess = true,
        ),
      )
    }

    @Test
    fun `should default limitedAccess to false when null`() {
      val case = buildCase().copy(limitedAccess = null)

      val result = toPersonDto(case, UserAccess.FULL)

      assertThat(result).isInstanceOf(FullPersonDto::class.java)
      assertThat((result as FullPersonDto).limitedAccess).isFalse()
    }

    @Test
    fun `should return null riskLevel when rosh level code is unknown`() {
      val case = buildCase(roshLevel = buildRoshLevel(code = "UNKNOWN"))

      val result = toPersonDto(case, UserAccess.FULL)

      assertThat(result).isInstanceOf(FullPersonDto::class.java)
      assertThat((result as FullPersonDto).riskLevel).isNull()
    }

    @Test
    fun `should return null riskLevel when rosh level is missing`() {
      val case = buildCase().copy(roshLevel = null)

      val result = toPersonDto(case, UserAccess.FULL)

      assertThat(result).isInstanceOf(FullPersonDto::class.java)
      assertThat((result as FullPersonDto).riskLevel).isNull()
    }

    @Test
    fun `should transform limited case to LimitedPersonDto`() {
      val case = buildCase(
        crn = "X99999",
        nomsNumber = "B4321CD",
        staff = buildOfficer(
          name = buildName(),
          username = "user.name",
        ),
        team = buildCaseTeam(code = "TEAM2"),
      )

      val result = toPersonDto(case, UserAccess.LIMITED)

      assertThat(result).isEqualTo(
        LimitedPersonDto(
          crn = "X99999",
          nomsNumber = "B4321CD",
          teamCode = "TEAM2",
          assignedTo = AssignedToDto(
            forename = "First",
            surname = "Last",
            username = "user.name",
          ),
        ),
      )
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(UserAccess::class, names = ["LIMITED", "UNKNOWN"])
    fun `should transform to LimitedPersonDto when access is missing, unknown or limited`(userAccess: UserAccess?) {
      val case = buildCase(crn = "X77777", nomsNumber = "C1234DE", team = buildCaseTeam(code = "TEAM3"))

      val result = toPersonDto(case, userAccess)

      assertThat(result).isInstanceOf(LimitedPersonDto::class.java)
      assertThat(result.crn).isEqualTo("X77777")
      assertThat(result.teamCode).isEqualTo("TEAM3")
    }
  }
}
