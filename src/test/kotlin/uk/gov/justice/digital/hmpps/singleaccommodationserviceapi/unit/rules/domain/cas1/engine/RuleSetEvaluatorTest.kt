package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.domain.cas1.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleSetEvaluator

class RuleSetEvaluatorTest {
  @Test
  fun `default rule set evaluator passes`() {
    val data = DomainData("A1")
    val ruleSet = Cas1RuleSet()
    val defaultRuleEvaluator = DefaultRuleEvaluator()
    val result = DefaultRuleSetEvaluator(defaultRuleEvaluator).evaluate(ruleSet, data)

    val expectedResult = listOf(RuleResult(STierRule().description, RuleStatus.PASS))

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `default rule set evaluator fails`() {
    val data = DomainData("A1S")
    val ruleSet = Cas1RuleSet()
    val defaultRuleEvaluator = DefaultRuleEvaluator()
    val result = DefaultRuleSetEvaluator(defaultRuleEvaluator).evaluate(ruleSet, data)

    val expectedResult = listOf(RuleResult(STierRule().description, RuleStatus.FAIL))

    assertThat(result).isEqualTo(expectedResult)
  }

}