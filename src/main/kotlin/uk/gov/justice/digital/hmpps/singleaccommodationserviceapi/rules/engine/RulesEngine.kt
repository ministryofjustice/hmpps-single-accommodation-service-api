package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus

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
        val failedResultsWithoutGuidance = failedResults.filter { !it.isGuidance }
        if (failedResultsWithoutGuidance.isEmpty()) {
          RuleSetStatus.GUIDANCE_FAIL
        } else {
          RuleSetStatus.FAIL
        }
      },
    )
  }
}
