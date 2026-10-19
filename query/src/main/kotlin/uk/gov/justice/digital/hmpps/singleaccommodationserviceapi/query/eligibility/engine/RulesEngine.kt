package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

class RulesEngine(
  private val evaluator: RuleSetEvaluator,
) {
  fun execute(ruleset: RuleSet, data: DomainData): RuleSetResult {
    val failedResults = evaluator.evaluate(ruleset, data)
      .filter { it.ruleResult.ruleStatus == RuleStatus.FAIL }

    return RuleSetResult(
      status = if (failedResults.isEmpty()) RuleSetStatus.PASS else RuleSetStatus.FAIL,
      failureReasons = findHighestPriorityFailureReasons(failedResults),
    )
  }

  private fun findHighestPriorityFailureReasons(
    failedResults: List<PrioritisedRuleResult>,
  ): List<FailureReason> {
    val relevantFailedResults = failedResults.filter { it.priority != -1 }
    val highestPriority = relevantFailedResults.minOfOrNull { it.priority }
    return relevantFailedResults.filter { it.priority == highestPriority }.mapNotNull { it.ruleResult.failureReason }
  }
}
