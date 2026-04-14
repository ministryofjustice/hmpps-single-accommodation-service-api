package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.eligibility.CurrentAddressTypeNotPrivateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCurrentAccommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class CurrentAddressTypeNotPrivateRuleTest {

  @Test
  fun `candidate passes when current address is not private`() {
    val data = buildDomainData(
      currentAccommodation = buildCurrentAccommodation(isPrivate = false),
    )

    val result = CurrentAddressTypeNotPrivateRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when current address is private`() {
    val data = buildDomainData(
      currentAccommodation = buildCurrentAccommodation(isPrivate = true),

    )

    val result = CurrentAddressTypeNotPrivateRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate passes when current address is missing`() {
    val data = buildDomainData(
      currentAccommodation = null,
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
