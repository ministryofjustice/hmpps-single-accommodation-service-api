package unit.client.corepersonrecord

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord

class CorePersonRecordTest {

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
