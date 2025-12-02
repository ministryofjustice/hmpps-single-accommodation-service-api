package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.FemaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.MaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.ReferralTimingGuidanceRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.CircuitBreakRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleSetEvaluator
import java.time.OffsetDateTime

class RuleSetEvaluatorTest {
  private val sTierRule = STierRule()
  private val maleRiskRule = MaleRiskRule()
  private val femaleRiskRule = FemaleRiskRule()
  private val referralTimingGuidanceRule = ReferralTimingGuidanceRule()
  val ruleSet = Cas1RuleSet()
  private val female = Sex(
    code = "F",
    description = "Female",
  )
  private val male = Sex(
    code = "M",
    description = "Male",
  )

  @Nested
  inner class DefaultRuleSetEvaluatorTests {
    private val defaultRuleSetEvaluator = DefaultRuleSetEvaluator()

    @Test
    fun `default rule set evaluator everything passes (male)`() {
      val data = DomainData(
        tier = "A1",
        sex = male,
        releaseDate = OffsetDateTime.now().plusMonths(6),
      )

      val result = defaultRuleSetEvaluator.evaluate(ruleSet, data)

      val expectedResult = listOf(
        RuleResult(sTierRule.description, RuleStatus.PASS, false),
        RuleResult(maleRiskRule.description, RuleStatus.PASS, false),
        RuleResult(femaleRiskRule.description, RuleStatus.PASS, false),
        RuleResult(referralTimingGuidanceRule.description, RuleStatus.PASS, true),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `default rule set evaluator nearly everything fails (female)`() {
      val data = DomainData(
        tier = "C2S",
        sex = female,
        releaseDate = OffsetDateTime.now().plusMonths(2),
      )
      val result = defaultRuleSetEvaluator.evaluate(ruleSet, data)

      val expectedResult = listOf(
        RuleResult(sTierRule.description, RuleStatus.FAIL, false),
        RuleResult(maleRiskRule.description, RuleStatus.PASS, false),
        RuleResult(femaleRiskRule.description, RuleStatus.FAIL, false),
        RuleResult(referralTimingGuidanceRule.description, RuleStatus.FAIL, true),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `default rule set evaluator first fails, second passes`() {
      val data = DomainData(
        tier = "A1S",
        sex = male,
        releaseDate = OffsetDateTime.now().plusMonths(6),
      )
      val result = defaultRuleSetEvaluator.evaluate(ruleSet, data)

      val expectedResult = listOf(
        RuleResult(sTierRule.description, RuleStatus.FAIL, false),
        RuleResult(maleRiskRule.description, RuleStatus.PASS, false),
        RuleResult(femaleRiskRule.description, RuleStatus.PASS, false),
        RuleResult(referralTimingGuidanceRule.description, RuleStatus.PASS, true),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `default rule set evaluator first passes, second fails`() {
      val data = DomainData(
        tier = "C1",
        sex = female,
        releaseDate = OffsetDateTime.now().plusMonths(3),
      )
      val result = defaultRuleSetEvaluator.evaluate(ruleSet, data)

      val expectedResult = listOf(
        RuleResult(sTierRule.description, RuleStatus.PASS, false),
        RuleResult(maleRiskRule.description, RuleStatus.PASS, false),
        RuleResult(femaleRiskRule.description, RuleStatus.FAIL, false),
        RuleResult(referralTimingGuidanceRule.description, RuleStatus.FAIL, true),
      )

      assertThat(result).isEqualTo(expectedResult)
    }
  }

  @Nested
  inner class CircuitBreakerRuleSetEvaluatorTests {
    private val circuitBreakRuleSetEvaluator = CircuitBreakRuleSetEvaluator()

    @Test
    fun `circuit breaker rule set evaluator everything passes`() {
      val data = DomainData(
        tier = "A1",
        sex = male,
        releaseDate = OffsetDateTime.now().plusMonths(6),
      )

      val result = circuitBreakRuleSetEvaluator.evaluate(ruleSet, data)

      val expectedResult = listOf<RuleResult>()

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `circuit breaker rule set nearly evaluator everything fails`() {
      val data = DomainData(
        tier = "A1S",
        sex = female,
        releaseDate = OffsetDateTime.now().plusMonths(6),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(ruleSet, data)

      val expectedResult = listOf(
        RuleResult(sTierRule.description, RuleStatus.FAIL, false),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `circuit breaker rule set evaluator first fails, second passes`() {
      val data = DomainData(
        tier = "A1S",
        sex = male,
        releaseDate = OffsetDateTime.now().plusMonths(6),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(ruleSet, data)

      val expectedResult = listOf(
        RuleResult(sTierRule.description, RuleStatus.FAIL, false),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `circuit breaker rule set evaluator first passes, second fails`() {
      val data = DomainData(
        tier = "D1",
        sex = female,
        releaseDate = OffsetDateTime.now().plusMonths(6),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(ruleSet, data)

      val expectedResult = listOf(
        RuleResult(femaleRiskRule.description, RuleStatus.FAIL, false),
      )

      assertThat(result).isEqualTo(expectedResult)
    }
  }
}
