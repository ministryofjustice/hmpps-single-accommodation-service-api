package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import org.slf4j.LoggerFactory
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
  private val ruleSetName: String,
  private val ruleSet: RuleSet,
  private val engine: RulesEngine,
  private val onPass: DecisionNode,
  private val onFail: DecisionNode,
  private val contextUpdater: ContextUpdater,
) : DecisionNode {

  companion object {
    private val log = LoggerFactory.getLogger(RuleSetNode::class.java)
  }

  override fun eval(context: EvaluationContext): ServiceResult {
    log.debug("Executing RuleSet: $ruleSetName")

    val evaluation = engine.execute(ruleSet, context.data)
    log.debug("RuleSet {} status: {}", ruleSetName, evaluation.status)

    if (evaluation.status == RuleSetStatus.FAIL) {
      log.info(
        "RuleSet {} FAILED: {}",
        ruleSetName,
        evaluation.failures.joinToString(", ") { it.description },
      )
    }

    // Branch based on result: PASS returns current context unchanged, FAIL updates context
    return when (evaluation.status) {
      // On PASS, return current context without updating
      RuleSetStatus.PASS -> onPass.eval(context)
      // On FAIL, update context and continue to next node
      RuleSetStatus.FAIL -> onFail.eval(contextUpdater.update(context))
    }
  }
}