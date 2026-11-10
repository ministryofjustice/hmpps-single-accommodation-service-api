package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew

/** All nodes are Decision Nodes **/
sealed interface DecisionNode<in D> {
  /** Evaluates this node and returns the final ServiceResult. */
  fun eval(context: EvaluationContext<D>): ServiceResultNew
}

/**
 * Terminal node that returns a ServiceResult based on the current context.
 *
 * [key] is a stable identifier (e.g. a status enum name) used for graph node ids; [name] is the human-readable label,
 * which may be shared by several distinct outcomes.
 **/
class OutcomeNode<in D>(
  val name: String = "unnamed",
  val key: String = name,
  private val outcome: (EvaluationContext<D>) -> ServiceResultNew,
) : DecisionNode<D> {
  override fun eval(context: EvaluationContext<D>): ServiceResultNew = outcome(context)
}

/**
 * Node that executes a RuleSet and branches based on the result.
 */
class RuleSetNode<in D>(
  val ruleSetName: String,
  val ruleSet: RuleSet<D>,
  private val engine: RulesEngine,
  val onPass: DecisionNode<D>,
  val onPassContextUpdater: ContextUpdater<D>?,
  val onFail: DecisionNode<D>,
  val onFailContextUpdater: ContextUpdater<D>?,
) : DecisionNode<D> {

  companion object {
    private val log = LoggerFactory.getLogger(RuleSetNode::class.java)
  }

  override fun eval(context: EvaluationContext<D>): ServiceResultNew {
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
