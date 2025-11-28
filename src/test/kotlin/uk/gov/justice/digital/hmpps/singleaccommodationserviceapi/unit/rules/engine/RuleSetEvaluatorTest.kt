package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.engine

import org.assertj.core.api.Assertions.assertThat
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
import java.time.LocalDate

class RuleSetEvaluatorTest {
  private val sTierRule = STierRule()
  private val maleRiskRule = MaleRiskRule()
  private val femaleRiskRule = FemaleRiskRule()
  private val referralTimingGuidanceRule = ReferralTimingGuidanceRule()
  private val defaultRuleSetEvaluator = DefaultRuleSetEvaluator()
  private val circuitBreakRuleSetEvaluator = CircuitBreakRuleSetEvaluator()
  val ruleSet = Cas1RuleSet()
  private val female = Sex(
    code = "F",
    description = "Female",
  )
  private val male = Sex(
    code = "M",
    description = "Male",
  )

  @Test
  fun `default rule set evaluator everything passes (male)`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      referralDate = null,
      releaseDate = LocalDate.now().plusMonths(6),
    )

    val result = defaultRuleSetEvaluator.evaluate(ruleSet, data)

    val expectedResult = listOf(
      RuleResult(sTierRule.description, RuleStatus.PASS),
      RuleResult(maleRiskRule.description, RuleStatus.PASS),
      RuleResult(referralTimingGuidanceRule.description, RuleStatus.PASS),
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `default rule set evaluator everything fails (female)`() {
    val data = DomainData(
      tier = "C2S",
      sex = female,
      referralDate = null,
      releaseDate = LocalDate.now().plusMonths(2),
    )
    val result = defaultRuleSetEvaluator.evaluate(ruleSet, data)

    val expectedResult = listOf(
      RuleResult(sTierRule.description, RuleStatus.FAIL),
      RuleResult(femaleRiskRule.description, RuleStatus.FAIL),
      RuleResult(referralTimingGuidanceRule.description, RuleStatus.GUIDANCE),
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `default rule set evaluator first fails, second passes`() {
    val data = DomainData(
      tier = "A1S",
      sex = male,
      referralDate = null,
      releaseDate = LocalDate.now().plusMonths(6),
    )
    val result = defaultRuleSetEvaluator.evaluate(ruleSet, data)

    val expectedResult = listOf(
      RuleResult(sTierRule.description, RuleStatus.FAIL),
      RuleResult(maleRiskRule.description, RuleStatus.PASS),
      RuleResult(referralTimingGuidanceRule.description, RuleStatus.PASS),
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `default rule set evaluator first passes, second fails`() {
    val data = DomainData(
      tier = "C1",
      sex = female,
      referralDate = null,
      releaseDate = LocalDate.now().plusMonths(3),
    )
    val result = defaultRuleSetEvaluator.evaluate(ruleSet, data)

    val expectedResult = listOf(
      RuleResult(sTierRule.description, RuleStatus.PASS),
      RuleResult(femaleRiskRule.description, RuleStatus.FAIL),
      RuleResult(referralTimingGuidanceRule.description, RuleStatus.GUIDANCE),
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `circuit breaker rule set evaluator everything passes`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      referralDate = null,
      releaseDate = LocalDate.now().plusMonths(6),
    )

    val result = circuitBreakRuleSetEvaluator.evaluate(ruleSet, data)

    val expectedResult = listOf<RuleResult>()

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `circuit breaker rule set evaluator everything fails`() {
    val data = DomainData(
      tier = "A1S",
      sex = female,
      referralDate = null,
      releaseDate = LocalDate.now().plusMonths(6),
    )
    val result = circuitBreakRuleSetEvaluator.evaluate(ruleSet, data)

    val expectedResult = listOf(
      RuleResult(sTierRule.description, RuleStatus.FAIL),
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `circuit breaker rule set evaluator first fails, second passes`() {
    val data = DomainData(
      tier = "A1S",
      sex = male,
      referralDate = null,
      releaseDate = LocalDate.now().plusMonths(6),
    )
    val result = circuitBreakRuleSetEvaluator.evaluate(ruleSet, data)

    val expectedResult = listOf(
      RuleResult(sTierRule.description, RuleStatus.FAIL),
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `circuit breaker rule set evaluator first passes, second fails`() {
    val data = DomainData(
      tier = "D1",
      sex = female,
      referralDate = null,
      releaseDate = LocalDate.now().plusMonths(6),
    )
    val result = circuitBreakRuleSetEvaluator.evaluate(ruleSet, data)

    val expectedResult = listOf(
      RuleResult(femaleRiskRule.description, RuleStatus.FAIL),
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `male risk rule is not evaluated for female data`() {
    val data = DomainData(
      tier = "A1",
      sex = female,
      referralDate = null,
      releaseDate = LocalDate.now().plusMonths(6),
    )
    val result = defaultRuleSetEvaluator.evaluate(ruleSet, data)
    assertThat(result.any { it.description == maleRiskRule.description }).isFalse()
  }

  @Test
  fun `female risk rule is not evaluated for male data`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      referralDate = null,
      releaseDate = LocalDate.now().plusMonths(6),
    )
    val result = defaultRuleSetEvaluator.evaluate(ruleSet, data)
    assertThat(result.any { it.description == femaleRiskRule.description }).isFalse()
  }
}
