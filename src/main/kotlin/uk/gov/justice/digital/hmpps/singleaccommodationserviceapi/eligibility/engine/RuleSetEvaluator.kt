package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.RuleStatus

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
