package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.CircuitBreakRuleSetEvaluator
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

  @Nested
  inner class CircuitBreakerRuleSetEvaluatorTests {

    val circuitBreakRuleSetEvaluator = CircuitBreakRuleSetEvaluator()

    @Test
    fun `circuit breaker rule set evaluator everything passes, don't return any results`() {
      val rule1AlwaysPass = AlwaysPassRule()
      val rule2AlwaysPass = AlwaysPassRule()
      val rule3AlwaysPass = AlwaysPassRule()

      val result = circuitBreakRuleSetEvaluator.evaluate(
        ruleset = StubRuleSet(
          listOf(
            rule1AlwaysPass,
            rule2AlwaysPass,
            rule3AlwaysPass,
          ),
        ),
        data = buildDomainData(),
      )

      val expectedResult = listOf<RuleResult>()

      assertThat(result).isEqualTo(expectedResult)

      assertThat(rule1AlwaysPass.hasBeenEvaluated).isTrue()
      assertThat(rule2AlwaysPass.hasBeenEvaluated).isTrue()
      assertThat(rule3AlwaysPass.hasBeenEvaluated).isTrue()
    }

    @Test
    fun `circuit breaker rule set evaluator multiple potential failures, stop after first failing rule`() {
      val rule1AlwaysFail = AlwaysFailRule()
      val rule2AlwaysFail = AlwaysFailRule()
      val rule3AlwaysPass = AlwaysPassRule()

      val result = circuitBreakRuleSetEvaluator.evaluate(
        ruleset = StubRuleSet(
          listOf(
            rule1AlwaysFail,
            rule2AlwaysFail,
            rule3AlwaysPass,
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
      )

      assertThat(result).isEqualTo(expectedResult)

      assertThat(rule1AlwaysFail.hasBeenEvaluated).isTrue()
      assertThat(rule2AlwaysFail.hasBeenEvaluated).isFalse()
      assertThat(rule3AlwaysPass.hasBeenEvaluated).isFalse()
    }

    @Test
    fun `circuit breaker rule set evaluator first fails, don't run additional rules`() {
      val rule1AlwaysFail = AlwaysFailRule()
      val rule2AlwaysPass = AlwaysPassRule()

      val result = circuitBreakRuleSetEvaluator.evaluate(
        ruleset = StubRuleSet(
          listOf(
            rule1AlwaysFail,
            rule2AlwaysPass,
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
      )

      assertThat(result).isEqualTo(expectedResult)

      assertThat(rule1AlwaysFail.hasBeenEvaluated).isTrue()
      assertThat(rule2AlwaysPass.hasBeenEvaluated).isFalse()
    }

    @Test
    fun `circuit breaker rule set evaluator first passes, second fails`() {
      val rule1AlwaysPass = AlwaysPassRule()
      val rule2AlwaysFail = AlwaysFailRule()

      val result = circuitBreakRuleSetEvaluator.evaluate(
        ruleset = StubRuleSet(
          listOf(
            rule1AlwaysPass,
            rule2AlwaysFail,
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
      )

      assertThat(result).isEqualTo(expectedResult)

      assertThat(rule1AlwaysPass.hasBeenEvaluated).isTrue()
      assertThat(rule2AlwaysFail.hasBeenEvaluated).isTrue()
    }
  }
}
