package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.TierNotAToDRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class TierNotAToDRuleTest {
  private val description = "FAIL if the person's tier is not A, B, C or D"

  @ParameterizedTest
  @ValueSource(strings = ["A", "B", "C", "D"])
  fun `high risk tier passes`(tierScore: String) {
    val result = TierNotAToDRule().evaluate(buildDomainData(tierScore = tierScore))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = ["E", "F", "G", "A1", "a", " A", "UNKNOWN"])
  fun `low risk or invalid tier fails with reason`(tierScore: String?) {
    val result = TierNotAToDRule().evaluate(buildDomainData(tierScore = tierScore))

    assertThat(result).isEqualTo(
      RuleResult(description, RuleStatus.FAIL, FailureReason.NON_MALE_NOT_HIGH_RISK_TIER),
    )
  }
}
