package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

class RulesEngine(
  private val evaluator: RuleSetEvaluator,
) {
  fun execute(ruleset: RuleSet, data: DomainData): RuleSetResult {
    val failedResults = evaluator.evaluate(ruleset, data)
      .filter { it.ruleStatus == RuleStatus.FAIL }

    return RuleSetResult(
      status = if (failedResults.isEmpty()) RuleSetStatus.PASS else RuleSetStatus.FAIL,
      failureReasons = failedResults.mapNotNull { it.failureReason },
    )
  }
}
