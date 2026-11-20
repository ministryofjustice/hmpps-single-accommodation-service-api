package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.TierNotAToCRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class TierNotAToCRuleTest {
  private val description = "FAIL if the person's tier is not A, B or C"

  @ParameterizedTest
  @ValueSource(strings = ["A", "B", "C"])
  fun `high risk tier passes`(tierScore: String) {
    val result = TierNotAToCRule().evaluate(buildDomainData(tierScore = tierScore))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = ["D", "E", "F", "G", "A1", "a", " A", "UNKNOWN"])
  fun `low risk or invalid tier fails with reason`(tierScore: String?) {
    val result = TierNotAToCRule().evaluate(buildDomainData(tierScore = tierScore))

    assertThat(result).isEqualTo(
      RuleResult(description, RuleStatus.FAIL, FailureReason.MALE_NOT_HIGH_RISK_TIER),
    )
  }
}
