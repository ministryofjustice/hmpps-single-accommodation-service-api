package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.pa.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.completion.HasNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class HasNextAccommodationRuleTest {
  @Test
  fun `candidate passes when nextAccommodation is present`() {
    val data = buildDomainData(
      nextAccommodation = buildAccommodationSummaryDto(),
    )

    val result = HasNextAccommodationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when nextAccommodation is missing`() {
    val data = buildDomainData(
      nextAccommodation = null,
    )

    val result = HasNextAccommodationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(HasNextAccommodationRule().description)
      .isEqualTo("FAIL if candidate has no next accommodation")
  }
}
