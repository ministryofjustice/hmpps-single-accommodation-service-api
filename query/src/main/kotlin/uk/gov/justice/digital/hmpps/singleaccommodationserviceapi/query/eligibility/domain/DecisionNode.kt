package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

/** All nodes are Decision Nodes **/
  sealed interface DecisionNode {
    /** Evaluates this node and returns the final ServiceResult. */
    fun eval(ctx: EvalContext): ServiceResult
  }

  /** Terminal node that returns a ServiceResult based on the current context.**/
  data class OutcomeNode(private val outcome: (EvalContext) -> ServiceResult) : DecisionNode {
    override fun eval(ctx: EvalContext): ServiceResult = outcome(ctx)
  }

  /**
   * Node that executes a RuleSet and branches based on the result.
   */
  data class RuleSetNode(
    private val name: String,
    private val ruleSet: RuleSet,
    private val engine: RulesEngine,
    private val onPass: DecisionNode,
    private val onFail: DecisionNode,
    private val contextUpdater: ContextUpdater,
  ) : DecisionNode {

    override fun eval(ctx: EvalContext): ServiceResult {
      val exec = engine.execute(ruleSet, ctx.data)

      // Branch based on result: PASS returns current context unchanged, FAIL updates context
      return if (exec == RuleSetStatus.PASS) {
        // On PASS, return current context without updating
        onPass.eval(ctx)
      } else {
        // On FAIL, update context and continue to next node
        val nextCtx = contextUpdater.update(ctx)
        onFail.eval(nextCtx)
      }
    }
  }