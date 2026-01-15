package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

/**
 * Generic builder for constructing decision trees. Allows declaratively to chain rulesets and outcomes.
 */
class DecisionTreeBuilder(
  private val engine: RulesEngine,
  ) {
  /**
   * Starts building a RuleSetNode with the given ruleset and context updater. Returns a RuleSetNodeBuilder
   */
  fun ruleSet(
    name: String,
    ruleSet: RuleSet,
    contextUpdater: ContextUpdater
  ): RuleSetNodeBuilder {
    return RuleSetNodeBuilder(name, ruleSet, contextUpdater, engine)
  }

  /** Creates a terminal outcome node that returns a fixed ServiceResult. */
  fun outcome(result: ServiceResult): DecisionNode {
    return OutcomeNode { _ -> result }
  }

  /** Creates a terminal outcome node that returns the current context's ServiceResult. */
  fun confirmed(): DecisionNode {
    return OutcomeNode { ctx -> ctx.current }
  }

  /** Creates a terminal outcome node for NOT_ELIGIBLE status */
  fun notEligible(): DecisionNode {
    return outcome(
      ServiceResult(
        serviceStatus =
          ServiceStatus.NOT_ELIGIBLE,
        suitableApplicationId = null,
      )
    )
  }
}
