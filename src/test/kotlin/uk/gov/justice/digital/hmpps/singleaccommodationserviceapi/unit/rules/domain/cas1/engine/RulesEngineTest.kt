package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.domain.cas1.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.FinalResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.RulesEngine

class RulesEngineTest {
  @Test
  fun `rules engine passes cas1 rules`() {
    val ruleSet = Cas1RuleSet()
    val data = DomainData("A1")
    val ruleEvaluator = DefaultRuleEvaluator()
    val evaluator = DefaultRuleSetEvaluator(ruleEvaluator)

    val result = RulesEngine(evaluator).execute(ruleSet, data)

    val expectedResult = FinalResult(listOf(), RuleSetStatus.PASS)

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `rules engine fails cas1 rules`() {
    val ruleSet = Cas1RuleSet()
    val data = DomainData("A1S")
    val ruleEvaluator = DefaultRuleEvaluator()
    val evaluator = DefaultRuleSetEvaluator(ruleEvaluator)

    val result = RulesEngine(evaluator).execute(ruleSet, data)

    val expectedResult = FinalResult(listOf(RuleResult(STierRule().description, RuleStatus.FAIL)), RuleSetStatus.FAIL)

    assertThat(result).isEqualTo(expectedResult)
  }

}