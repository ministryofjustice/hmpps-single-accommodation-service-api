package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

class RulesEngine(
  private val evaluator: RuleSetEvaluator,
) {
  fun execute(ruleset: RuleSet, data: DomainData): RuleSetResult {
    val results = evaluator.evaluate(ruleset, data)
    return aggregateResults(results)
  }

  private fun aggregateResults(results: List<RuleResult>): RuleSetResult {
    val hasFail = results.any { it.ruleStatus == RuleStatus.FAIL }
    val hasNonGuidanceFail = results.any { it.ruleStatus == RuleStatus.FAIL && !it.actionable }

    return when {
      !hasFail ->
        RuleSetResult(
          actions = emptyList(),
          ruleSetStatus = RuleSetStatus.PASS,
        )

      !hasNonGuidanceFail -> RuleSetResult(
        actions = results.mapNotNull { it.potentialAction },
        ruleSetStatus = RuleSetStatus.ACTIONABLE_FAIL,
      )

      else ->  RuleSetResult(
        actions = emptyList(),
        ruleSetStatus = RuleSetStatus.FAIL,
      )
    }
  }
}
