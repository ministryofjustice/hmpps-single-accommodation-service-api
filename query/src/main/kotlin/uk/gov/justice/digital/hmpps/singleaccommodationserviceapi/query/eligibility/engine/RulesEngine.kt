package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

class RulesEngine(
  private val evaluator: RuleSetEvaluator,
) {
  fun <D> execute(ruleset: RuleSet<D>, data: D): RuleSetResult {
    val failedResults = evaluator.evaluate(ruleset, data)
      .filter { it.ruleStatus == RuleStatus.FAIL }

    return RuleSetResult(
      status = if (failedResults.isEmpty()) RuleSetStatus.PASS else RuleSetStatus.FAIL,
      failureReasons = failedResults.mapNotNull { it.failureReason },
    )
  }
}
