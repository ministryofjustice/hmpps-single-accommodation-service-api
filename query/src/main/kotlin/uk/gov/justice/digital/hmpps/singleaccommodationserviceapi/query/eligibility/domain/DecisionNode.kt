package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

/** All nodes are Decision Nodes **/
sealed interface DecisionNode {
  /** Evaluates this node and returns the final ServiceResult. */
  fun eval(context: EvaluationContext): ServiceResultNew
}

/**
 * Terminal node that returns a ServiceResult based on the current context.
 *
 * [key] is a stable identifier (e.g. a status enum name) used for graph node ids; [name] is the human-readable label,
 * which may be shared by several distinct outcomes.
 **/
class OutcomeNode(
  val name: String = "unnamed",
  val key: String = name,
  private val outcome: (EvaluationContext) -> ServiceResultNew,
) : DecisionNode {
  override fun eval(context: EvaluationContext): ServiceResultNew = outcome(context)
}

/**
 * Node that executes a RuleSet and branches based on the result.
 */
class RuleSetNode(
  val ruleSetName: String,
  val ruleSet: RuleSet,
  private val engine: RulesEngine,
  val onPass: DecisionNode,
  val onPassContextUpdater: ContextUpdater?,
  val onFail: DecisionNode,
  val onFailContextUpdater: ContextUpdater?,
) : DecisionNode {

  companion object {
    private val log = LoggerFactory.getLogger(RuleSetNode::class.java)
  }

  override fun eval(context: EvaluationContext): ServiceResultNew {
    log.debug("Executing RuleSet: $ruleSetName")

    val ruleSetResult = engine.execute(ruleSet, context.data).also {
      log.debug("RuleSet Result: {}", it.status)
    }

    return when (ruleSetResult.status) {
      RuleSetStatus.PASS -> onPass.eval(onPassContextUpdater?.update(context, ruleSetResult.failureReasons) ?: context)
      RuleSetStatus.FAIL -> onFail.eval(
        onFailContextUpdater?.update(context, ruleSetResult.failureReasons)
          ?: context.withFailureReasons(ruleSetResult.failureReasons),
      )
    }
  }
}
