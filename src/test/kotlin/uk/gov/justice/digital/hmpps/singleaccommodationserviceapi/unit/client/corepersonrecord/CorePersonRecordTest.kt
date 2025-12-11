package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.client.corepersonrecord

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Identifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.SexCode
import java.util.UUID

class CorePersonRecordTest {

  @Test
  fun `getPrisonerNumberFromCprData returns last prison number`() {
    val prisonNumbers = listOf("PN1", "PN2")
    val cpr = CorePersonRecord(sex = Sex(SexCode.M, "Male"), identifiers = Identifiers(prisonNumbers = prisonNumbers))
    val result = cpr.getPrisonNumber()
    assertThat(result).isEqualTo(prisonNumbers.last())
  }

  @Test
  fun `getPrisonerNumberFromCprData throws error when prisonNumbers is null or empty`() {
    val cases = listOf(
      CorePersonRecord(
        cprUUID = UUID.randomUUID(),
        sex = Sex(SexCode.M, "Male"),
        identifiers = null,
      ),
      CorePersonRecord(
        cprUUID = UUID.randomUUID(),
        sex = Sex(SexCode.M, "Male"),
        identifiers = Identifiers(prisonNumbers = emptyList()),
      ),
    )
    cases.forEach { cpr ->
      assertThatThrownBy { cpr.getPrisonNumber() }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessageContaining("No prisoner number found for cpr ${cpr.cprUUID}")
    }
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "John,Paul Andrew, Smith, John Paul Andrew Smith",
      "John, null, Smith, John Smith",
      "John, '', Smith, John Smith",
      "John, '       ', Smith, John Smith",
      "Firstname, MiddleName, null, Firstname MiddleName",
      "null, MiddleName, Lastname, MiddleName Lastname",
      "Alice, X, Wonderland, Alice X Wonderland",
      "Alice, X Y Z 1, Wonder land, Alice X Y Z 1 Wonder land",
      "null, null, null, ''",
    ],
    nullValues = ["null"],
  )
  fun `should format name correctly`(
    firstname: String?,
    middleNames: String?,
    lastName: String?,
    expected: String,
  ) {
    val cpr = CorePersonRecord(firstName = firstname, middleNames = middleNames, lastName = lastName)

    assertThat(cpr.fullName).isEqualTo(expected)
  }
}
