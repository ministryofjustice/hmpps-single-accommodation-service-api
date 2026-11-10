package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.WrongTierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class WrongTierRuleTest {
  private val description = "FAIL if person is not Tier A through G"

  @ParameterizedTest
  @EnumSource(TierScore::class, names = ["MISSING", "NOT_SUPERVISED"], mode = EnumSource.Mode.EXCLUDE)
  fun `person passes when tier score is a valid A to G value`(tierScore: TierScore) {
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
  @EnumSource(TierScore::class, names = ["MISSING", "NOT_SUPERVISED"])
  fun `person fails when tier score is missing or unsupervised`(tierScore: TierScore) {
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
