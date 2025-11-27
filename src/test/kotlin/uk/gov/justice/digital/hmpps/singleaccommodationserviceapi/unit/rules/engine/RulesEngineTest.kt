package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.FinalResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.MaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.CircuitBreakRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.RulesEngine

class RulesEngineTest {
  private val sTierRule = STierRule()
  private val maleRiskRule = MaleRiskRule()
  private val ruleEvaluator = DefaultRuleEvaluator()
  private val defaultRuleSetEvaluator = DefaultRuleSetEvaluator(ruleEvaluator)
  private val circuitBreakerRuleSetEvaluator = CircuitBreakRuleSetEvaluator(ruleEvaluator)
  val ruleSet = Cas1RuleSet()
  private val male = Sex(
    code = "M",
    description = "Male",
  )
  private val female = Sex(
    code = "F",
    description = "Female",
  )

  @Test
  fun `rules engine passes cas1 rules with default rule set evaluator`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
    )

    val result = RulesEngine(defaultRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = FinalResult(listOf(), RuleSetStatus.PASS)

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `rules engine fails all cas1 rules with default rule set evaluator`() {
    val data = DomainData(
      "A1S",
      sex = female,
    )

    val result = RulesEngine(defaultRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = FinalResult(
      failedResults = listOf(
        RuleResult(sTierRule.description, RuleStatus.FAIL),
        RuleResult(maleRiskRule.description, RuleStatus.FAIL),
      ),
      ruleSetStatus = RuleSetStatus.FAIL,
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `rules engine fail first cas1 rule with default rule set evaluatore`() {
    val data = DomainData(
      "A1S",
      sex = male,
    )

    val result = RulesEngine(defaultRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = FinalResult(
      failedResults = listOf(
        RuleResult(sTierRule.description, RuleStatus.FAIL),
      ),
      ruleSetStatus = RuleSetStatus.FAIL,
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `rules engine fail second cas1 rule with default rule set evaluator`() {
    val data = DomainData(
      "A1",
      sex = female,
    )

    val result = RulesEngine(defaultRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = FinalResult(
      failedResults = listOf(
        RuleResult(maleRiskRule.description, RuleStatus.FAIL),
      ),
      ruleSetStatus = RuleSetStatus.FAIL,
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `rules engine passes cas1 rules with circuit breaker rule set evaluator`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
    )

    val result = RulesEngine(circuitBreakerRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = FinalResult(listOf(), RuleSetStatus.PASS)

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `rules engine fails all cas1 rules with circuit breaker rule set evaluator`() {
    val data = DomainData(
      "A1S",
      sex = female,
    )

    val result = RulesEngine(circuitBreakerRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = FinalResult(
      failedResults = listOf(
        RuleResult(sTierRule.description, RuleStatus.FAIL),
      ),
      ruleSetStatus = RuleSetStatus.FAIL,
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `rules engine fail first cas1 rule with circuit breaker rule set evaluatore`() {
    val data = DomainData(
      "A1S",
      sex = male,
    )

    val result = RulesEngine(circuitBreakerRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = FinalResult(
      failedResults = listOf(
        RuleResult(sTierRule.description, RuleStatus.FAIL),
      ),
      ruleSetStatus = RuleSetStatus.FAIL,
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `rules engine fail second cas1 rule with circuit breaker rule set evaluator`() {
    val data = DomainData(
      "A1",
      sex = female,
    )

    val result = RulesEngine(circuitBreakerRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = FinalResult(
      failedResults = listOf(
        RuleResult(maleRiskRule.description, RuleStatus.FAIL),
      ),
      ruleSetStatus = RuleSetStatus.FAIL,
    )

    assertThat(result).isEqualTo(expectedResult)
  }
}
