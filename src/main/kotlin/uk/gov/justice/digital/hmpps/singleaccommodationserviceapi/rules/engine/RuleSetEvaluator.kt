package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus

interface RuleSetEvaluator {
  fun evaluate(ruleset: RuleSet, data: DomainData): List<RuleResult>
}

class DefaultRuleSetEvaluator(private val evaluator: RuleEvaluator) : RuleSetEvaluator {
  override fun evaluate(ruleset: RuleSet, data: DomainData): List<RuleResult> = ruleset.getRules().map { rule -> evaluator.evaluate(rule, data) }
}

class CircuitBreakRuleSetEvaluator(private val evaluator: RuleEvaluator) : RuleSetEvaluator {
  override fun evaluate(ruleset: RuleSet, data: DomainData): List<RuleResult> {
    ruleset.getRules().map { rule ->
      val result = evaluator.evaluate(rule, data)
      if (result.ruleStatus == RuleStatus.FAIL) {
        return listOf(result)
      }
    }
    return listOf()
  }
}
