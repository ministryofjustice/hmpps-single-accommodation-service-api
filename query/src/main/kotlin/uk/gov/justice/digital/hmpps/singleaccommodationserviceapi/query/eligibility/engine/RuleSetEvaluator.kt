package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

interface RuleSetEvaluator {
  fun evaluate(ruleset: RuleSet, data: DomainData): List<RuleResult>
}

class DefaultRuleSetEvaluator : RuleSetEvaluator {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun evaluate(ruleset: RuleSet, data: DomainData): List<RuleResult> = ruleset.getRules()
    .map { rule ->
      log.info("Evaluating rule: {}", rule.description)

      rule.evaluate(data).also { log.info("Rule result: {}", it.ruleStatus) }
    }
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
