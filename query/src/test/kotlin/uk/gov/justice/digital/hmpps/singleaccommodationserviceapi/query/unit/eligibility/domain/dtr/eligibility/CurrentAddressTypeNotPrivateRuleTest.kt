package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.eligibility.CurrentAddressTypeNotPrivateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class CurrentAddressTypeNotPrivateRuleTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = AccommodationArrangementType::class, mode = EnumSource.Mode.EXCLUDE, names = ["PRIVATE"])
  fun `candidate passes when current address is not private`(currentAccommodationArrangementType: AccommodationArrangementType) {
    val data = buildDomainData(
      currentAccommodationArrangementType = currentAccommodationArrangementType,
    )

    val result = CurrentAddressTypeNotPrivateRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when current address is private`() {
    val data = buildDomainData(
      currentAccommodationArrangementType = AccommodationArrangementType.PRIVATE,
    )

    val result = CurrentAddressTypeNotPrivateRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate passes when current address is missing`() {
    val data = buildDomainData(
      currentAccommodationArrangementType = null,
    )

    val result = CurrentAddressTypeNotPrivateRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(CurrentAddressTypeNotPrivateRule().description)
      .isEqualTo("FAIL if current address is Private")
  }
}
