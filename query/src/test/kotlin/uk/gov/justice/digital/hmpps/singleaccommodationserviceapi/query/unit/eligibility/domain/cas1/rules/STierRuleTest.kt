package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.STierEligibilityRule
import java.time.LocalDate

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
  fun `candidate passes`(tier: TierScore) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusYears(1),
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
  fun `candidate fails`(tier: TierScore) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusYears(1),
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
