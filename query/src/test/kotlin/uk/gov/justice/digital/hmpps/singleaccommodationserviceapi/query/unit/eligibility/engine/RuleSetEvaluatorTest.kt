package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.AlwaysFailRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.AlwaysPassRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubRuleSet

class RuleSetEvaluatorTest {

  @Nested
  inner class DefaultRuleSetEvaluatorTests {

    val defaultRuleSetEvaluator = DefaultRuleSetEvaluator()

    @Test
    fun `default rule set evaluator everything passes`() {
      val result = defaultRuleSetEvaluator.evaluate(
        ruleset = StubRuleSet(
          listOf(
            AlwaysPassRule(),
            AlwaysPassRule(),
            AlwaysPassRule(),
          ),
        ),
        data = buildDomainData(),
      )

      val expectedResult = listOf(
        RuleResult(
          description = "FAIL never",
          ruleStatus = RuleStatus.PASS,
          failureReason = null,
        ),
        RuleResult(
          description = "FAIL never",
          ruleStatus = RuleStatus.PASS,
          failureReason = null,
        ),
        RuleResult(
          description = "FAIL never",
          ruleStatus = RuleStatus.PASS,
          failureReason = null,
        ),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `default rule set evaluator nearly everything fails`() {
      val result = defaultRuleSetEvaluator.evaluate(
        ruleset = StubRuleSet(
          listOf(
            AlwaysFailRule(),
            AlwaysPassRule(),
            AlwaysFailRule(),
          ),
        ),
        data = buildDomainData(),
      )

      val expectedResult = listOf(
        RuleResult(
          description = "FAIL always",
          ruleStatus = RuleStatus.FAIL,
          failureReason = FailureReason.IS_SETTLED,
        ),
        RuleResult(
          description = "FAIL never",
          ruleStatus = RuleStatus.PASS,
          failureReason = null,
        ),
        RuleResult(
          description = "FAIL always",
          ruleStatus = RuleStatus.FAIL,
          failureReason = FailureReason.IS_SETTLED,
        ),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `default rule set evaluator first fails, second passes`() {
      val result = defaultRuleSetEvaluator.evaluate(
        ruleset = StubRuleSet(
          listOf(
            AlwaysFailRule(),
            AlwaysPassRule(),
          ),
        ),
        data = buildDomainData(),
      )

      val expectedResult = listOf(
        RuleResult(
          description = "FAIL always",
          ruleStatus = RuleStatus.FAIL,
          failureReason = FailureReason.IS_SETTLED,
        ),
        RuleResult(
          description = "FAIL never",
          ruleStatus = RuleStatus.PASS,
          failureReason = null,
        ),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `default rule set evaluator first passes, second fails`() {
      val result = defaultRuleSetEvaluator.evaluate(
        ruleset = StubRuleSet(
          listOf(
            AlwaysPassRule(),
            AlwaysFailRule(),
          ),
        ),
        data = buildDomainData(),
      )

      val expectedResult = listOf(
        RuleResult(
          description = "FAIL never",
          ruleStatus = RuleStatus.PASS,
          failureReason = null,
        ),
        RuleResult(
          description = "FAIL always",
          ruleStatus = RuleStatus.FAIL,
          failureReason = FailureReason.IS_SETTLED,
        ),
      )

      assertThat(result).isEqualTo(expectedResult)
    }
  }
}
