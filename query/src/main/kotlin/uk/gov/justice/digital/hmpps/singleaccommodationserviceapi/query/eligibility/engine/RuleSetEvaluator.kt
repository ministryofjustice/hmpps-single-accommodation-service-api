package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

import org.slf4j.LoggerFactory

interface RuleSetEvaluator {
  fun evaluate(
    ruleset: RuleSet,
    data: DomainData,
  ): List<RuleResult>
}

class DefaultRuleSetEvaluator : RuleSetEvaluator {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun evaluate(ruleset: RuleSet, data: DomainData): List<RuleResult> = ruleset.getRules()
    .map { rule ->
      log.debug("Evaluating rule: {}", rule.description)

      rule.evaluate(data).also {
        log.debug("Rule result: {}", it.ruleStatus)
      }
    }
}
