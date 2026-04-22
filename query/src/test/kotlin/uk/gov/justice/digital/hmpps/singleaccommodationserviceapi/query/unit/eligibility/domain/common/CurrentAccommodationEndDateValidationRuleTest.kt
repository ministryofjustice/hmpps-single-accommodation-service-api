package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CurrentAccommodationEndDateValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCurrentAccommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate

class CurrentAccommodationEndDateValidationRuleTest {
  @Test
  fun `candidate passes if current accommodation end date is present`() {
    val data = buildDomainData(
      currentAccommodation = buildCurrentAccommodation(LocalDate.now().plusYears(1)),
    )

    val result = CurrentAccommodationEndDateValidationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails if current accommodation end date is missing`() {
    val data = buildDomainData(
      currentAccommodation = null,
    )

    val result = CurrentAccommodationEndDateValidationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    val result = CurrentAccommodationEndDateValidationRule().description
    assertThat(result).isEqualTo("FAIL if candidate has no current accommodation end date")
  }
}
