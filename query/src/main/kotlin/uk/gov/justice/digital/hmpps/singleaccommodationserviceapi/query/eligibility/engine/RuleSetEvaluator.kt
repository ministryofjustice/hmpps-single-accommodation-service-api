package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

interface RuleSetEvaluator {
  fun evaluate(
    ruleset: RuleSet,
    data: DomainData,
  ): List<PrioritisedRuleResult>
}

class DefaultRuleSetEvaluator : RuleSetEvaluator {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun evaluate(ruleset: RuleSet, data: DomainData): List<PrioritisedRuleResult> = ruleset.getRules()
    .map { rule ->
      log.debug("Evaluating rule: {}", rule.description)

      rule.evaluate(data).also {
        log.debug("Rule result: {}", it.ruleResult.ruleStatus)
      }
    }
}
