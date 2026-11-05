package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class MaleRiskEligibilityRuleTest {
  private val description = "FAIL if person is not Tier A, B or C"

  @ParameterizedTest
  @ValueSource(strings = ["A", "B", "C"])
  fun `high risk tier passes`(tierScore: String) {
    val result = MaleRiskEligibilityRule().evaluate(buildDomainData(tierScore = tierScore))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = ["D", "E", "F", "G", "A1", "a", " A", "UNKNOWN"])
  fun `low risk or invalid tier fails with reason`(tierScore: String?) {
    val result = MaleRiskEligibilityRule().evaluate(buildDomainData(tierScore = tierScore))

    assertThat(result).isEqualTo(
      RuleResult(description, RuleStatus.FAIL, FailureReason.MALE_NOT_HIGH_RISK_TIER),
    )
  }
}
