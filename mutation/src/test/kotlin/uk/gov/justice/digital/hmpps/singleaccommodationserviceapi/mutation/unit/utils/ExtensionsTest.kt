package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.utils.isProposedAccommodationStatus

class ExtensionsTest {

  @ParameterizedTest
  @ValueSource(strings = ["PR", "PR1"])
  fun `isProposedAccommodationStatus returns true for proposed status codes`(statusCode: String) {
    assertThat(statusCode.isProposedAccommodationStatus()).isTrue()
  }

  @ParameterizedTest
  @EnumSource(value = AddressStatusCode::class, names = ["PR", "PR1"], mode = EnumSource.Mode.EXCLUDE)
  fun `isProposedAccommodationStatus returns false for all non-proposed status codes`(addressStatusCode: AddressStatusCode) {
    assertThat(addressStatusCode.name.isProposedAccommodationStatus()).isFalse()
  }

  @Test
  fun `isProposedAccommodationStatus returns false for null`() {
    val statusCode: String? = null

    assertThat(statusCode.isProposedAccommodationStatus()).isFalse()
  }

  @Test
  fun `isProposedAccommodationStatus returns false for a status code that does not exist`() {
    assertThat("NOT_A_REAL_STATUS_CODE".isProposedAccommodationStatus()).isFalse()
  }
}
