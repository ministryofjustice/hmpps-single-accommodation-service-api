package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.STierEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class STierRuleTest {
  private val crn = "ABC234"

  @ParameterizedTest(name = "{0}")
  @EnumSource(
    value = TierScore::class,
    names = [
      "A3",
      "A2",
      "A1",
      "B3",
      "B2",
      "B1",
      "C3",
      "C2",
      "C1",
      "D3",
      "D2",
      "D1",
    ],
  )
  fun `candidate passes`(tierScore: TierScore) {
    val data = buildDomainData(
      crn = crn,
      tierScore = tierScore,
    )

    val result = STierEligibilityRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
    value = TierScore::class,
    names = [
      "A3S",
      "A2S",
      "A1S",
      "B3S",
      "B2S",
      "B1S",
      "C3S",
      "C2S",
      "C1S",
      "D3S",
      "D2S",
      "D1S",
    ],
  )
  fun `candidate fails`(tierScore: TierScore) {
    val data = buildDomainData(
      crn = crn,
      tierScore = tierScore,
    )

    val result = STierEligibilityRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    val result = STierEligibilityRule().description
    assertThat(result).isEqualTo("FAIL if candidate is S Tier")
  }
}
