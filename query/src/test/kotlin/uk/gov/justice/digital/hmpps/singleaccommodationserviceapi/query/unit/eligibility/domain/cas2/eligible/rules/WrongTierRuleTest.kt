package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligible.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.WrongTierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class WrongTierRuleTest {
  private val description = "FAIL if person is not Tier A through G"

  @ParameterizedTest
  @ValueSource(strings = ["A", "B", "C", "D", "E", "F", "G"])
  fun `person passes when tier score is a valid A to G value`(tierScore: String) {
    val data = buildDomainData(
      tierScore = tierScore,
    )

    val result = WrongTierRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @ParameterizedTest
  @ValueSource(strings = ["MISSING", "NOT_SUPERVISED"])
  fun `person fails when tier score is missing or unsupervised`(tierScore: String) {
    val data = buildDomainData(
      tierScore = tierScore,
    )

    val result = WrongTierRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.INVALID_TIER,
      ),
    )
  }

  @Test
  fun `person fails when tier score is null`() {
    val data = buildDomainData(
      tierScore = null,
    )

    val result = WrongTierRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.INVALID_TIER,
      ),
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(WrongTierRule().description)
      .isEqualTo(description)
  }
}
