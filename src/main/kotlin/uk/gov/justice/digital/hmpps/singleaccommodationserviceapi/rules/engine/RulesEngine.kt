package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.FinalResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus

class RulesEngine(
  private val evaluator: RuleSetEvaluator,
) {
  fun execute(ruleset: RuleSet, data: DomainData): FinalResult {
    val results = evaluator.evaluate(ruleset, data)
    return aggregateResults(results)
  }

  private fun aggregateResults(results: List<RuleResult>): FinalResult {
    val failedResults = results.filter { it.ruleStatus == RuleStatus.FAIL }
    return FinalResult(
      failedResults = failedResults,
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
