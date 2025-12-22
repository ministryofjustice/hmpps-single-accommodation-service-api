package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus

class RulesEngine(
  private val evaluator: RuleSetEvaluator,
) {
  fun execute(ruleset: RuleSet, data: DomainData): ServiceResult {
    val results = evaluator.evaluate(ruleset, data)
    return aggregateResults(results)
  }

  private fun aggregateResults(results: List<RuleResult>): ServiceResult {
    val actions = results.mapNotNull { it.potentialAction }
    val hasFail = results.any { it.ruleStatus == RuleStatus.FAIL }
    val hasNonGuidanceFail = results.any { it.ruleStatus == RuleStatus.FAIL && !it.actionable }
    return when {
      !hasFail ->
        ServiceResult(
          actions = actions,
          serviceStatus = ServiceStatus.UPCOMING,
        )

      !hasNonGuidanceFail -> ServiceResult(
        actions = actions,
        serviceStatus = ServiceStatus.NOT_STARTED,
      )

      else -> ServiceResult(
        actions = listOf(),
        serviceStatus = ServiceStatus.NOT_ELIGIBLE,
      )
    }
  }
}
