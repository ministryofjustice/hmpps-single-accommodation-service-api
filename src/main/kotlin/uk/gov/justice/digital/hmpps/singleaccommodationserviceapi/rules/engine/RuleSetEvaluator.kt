package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus

interface RuleSetEvaluator {
  fun evaluate(ruleset: RuleSet, data: DomainData): List<RuleResult>
}

class DefaultRuleSetEvaluator : RuleSetEvaluator {
  override fun evaluate(ruleset: RuleSet, data: DomainData): List<RuleResult> = ruleset.getRules()
    .map { rule -> rule.evaluate(data) }
}

class CircuitBreakRuleSetEvaluator : RuleSetEvaluator {
  override fun evaluate(ruleset: RuleSet, data: DomainData): List<RuleResult> {
    ruleset.getRules()
      .map { rule ->
        val result = rule.evaluate(data)
        if (result.ruleStatus == RuleStatus.FAIL) {
          return listOf(result)
        }
      }
    return listOf()
  }
}
