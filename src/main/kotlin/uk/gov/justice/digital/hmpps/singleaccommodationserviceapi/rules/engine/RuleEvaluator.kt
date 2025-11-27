package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult

interface RuleEvaluator {
  fun evaluate(rule: Rule, data: DomainData): RuleResult
}

class DefaultRuleEvaluator : RuleEvaluator {
  override fun evaluate(rule: Rule, data: DomainData): RuleResult = rule.evaluate(data)
}
