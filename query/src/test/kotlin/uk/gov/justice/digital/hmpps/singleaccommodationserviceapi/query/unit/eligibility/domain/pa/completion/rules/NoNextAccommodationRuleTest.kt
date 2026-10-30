package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.pa.completion.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.referral.rules.NoNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class NoNextAccommodationRuleTest {
  @Test
  fun `person passes when nextAccommodation is present`() {
    val data = buildDomainData(
      nextAccommodations = listOf(buildAccommodationSummaryDto()),
    )

    val result = NoNextAccommodationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `person fails when nextAccommodation is missing`() {
    val data = buildDomainData(
      nextAccommodations = emptyList(),
    )

    val result = NoNextAccommodationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(NoNextAccommodationRule().description)
      .isEqualTo("FAIL if person has no future accommodation")
  }
}
