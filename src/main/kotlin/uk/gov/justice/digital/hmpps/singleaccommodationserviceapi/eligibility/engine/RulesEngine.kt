package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.RuleStatus

class RulesEngine(
  private val evaluator: RuleSetEvaluator,
) {
  fun execute(ruleset: RuleSet, data: DomainData): RuleSetResult {
    val results = evaluator.evaluate(ruleset, data)
    return aggregateResults(results)
  }

  private fun aggregateResults(results: List<RuleResult>): RuleSetResult {
    val failedResults = results.filter { it.ruleStatus == RuleStatus.FAIL }
    return RuleSetResult(
      results = results,
      ruleSetStatus = if (failedResults.isEmpty()) {
        RuleSetStatus.PASS
      } else {
        val failedResultsWithoutAction = failedResults.filter { !it.actionable }
        if (failedResultsWithoutAction.isEmpty()) {
          RuleSetStatus.ACTION_NEEDED
        } else {
          RuleSetStatus.FAIL
        }
      },
    )
  }
}
