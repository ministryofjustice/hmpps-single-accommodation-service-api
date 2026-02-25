package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

/** All nodes are Decision Nodes **/
sealed interface DecisionNode {
  /** Evaluates this node and returns the final ServiceResult. */
  fun eval(context: EvaluationContext): ServiceResult
}

/** Terminal node that returns a ServiceResult based on the current context.**/
class OutcomeNode(private val outcome: (EvaluationContext) -> ServiceResult) : DecisionNode {
  override fun eval(context: EvaluationContext): ServiceResult = outcome(context)
}

/**
 * Node that executes a RuleSet and branches based on the result.
 */
class RuleSetNode(
  private val ruleSet: RuleSet,
  private val engine: RulesEngine,
  private val onPass: DecisionNode,
  private val onFail: DecisionNode,
  private val contextUpdater: ContextUpdater,
) : DecisionNode {

  override fun eval(context: EvaluationContext): ServiceResult {
    val ruleSetStatus = engine.execute(ruleSet, context.data)

    // Branch based on result: PASS returns current context unchanged, FAIL updates context
    return when (ruleSetStatus) {
      // On PASS, return current context without updating
      RuleSetStatus.PASS -> onPass.eval(context)
      // On FAIL, update context and continue to next node
      RuleSetStatus.FAIL -> onFail.eval(contextUpdater.update(context))
    }
  }
}
