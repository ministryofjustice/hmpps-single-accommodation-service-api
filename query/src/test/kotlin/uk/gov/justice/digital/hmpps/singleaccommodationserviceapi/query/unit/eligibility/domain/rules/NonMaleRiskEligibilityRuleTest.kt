package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.NullSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class NonMaleRiskEligibilityRuleTest {
  private val description = "FAIL if person is not Tier A, B, C or D"

  @ParameterizedTest
  @EnumSource(TierScore::class, names = ["A", "B", "C", "D"])
  fun `high risk tier passes`(tierScore: TierScore) {
    val result = NonMaleRiskEligibilityRule().evaluate(buildDomainData(tierScore = tierScore))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @ParameterizedTest
  @NullSource
  @EnumSource(TierScore::class, names = ["A", "B", "C", "D"], mode = EnumSource.Mode.EXCLUDE)
  fun `low risk or unavailable tier fails with reason`(tierScore: TierScore?) {
    val result = NonMaleRiskEligibilityRule().evaluate(buildDomainData(tierScore = tierScore))

    assertThat(result).isEqualTo(
      RuleResult(description, RuleStatus.FAIL, FailureReason.NON_MALE_NOT_HIGH_RISK_TIER),
    )
  }
}
