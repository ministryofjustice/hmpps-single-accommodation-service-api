package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

class RuleSetNodeBuilder(
  private var name: String,
  private val ruleSet: RuleSet,
  private val engine: RulesEngine,
) {
  private var contextUpdater: ContextUpdater? = null
  private var onPassNode: DecisionNode? = null
  private var onFailNode: DecisionNode? = null

  fun onPass(node: DecisionNode): RuleSetNodeBuilder {
    onPassNode = node
    return this
  }

  fun onFail(node: DecisionNode): RuleSetNodeBuilder {
    onFailNode = node
    return this
  }

  fun applyContextUpdaterOnFailure(updater: ContextUpdater): RuleSetNodeBuilder {
    contextUpdater = updater
    return this
  }

  fun propagateContextOnFailure(): RuleSetNodeBuilder {
    contextUpdater = ContextUpdater.identity()
    return this
  }

  fun setConstantServiceResultOnFailure(result: ServiceResultNew): RuleSetNodeBuilder {
    contextUpdater = ContextUpdater.constant(result)
    return this
  }

  fun continueWith(node: DecisionNode): RuleSetNodeBuilder = onPass(node).onFail(node)

  fun build(): RuleSetNode = RuleSetNode(
    ruleSetName = name,
    ruleSet = ruleSet,
    engine = engine,
    onPass = requireNotNull(onPassNode) { "onPass node not set for RuleSetNode $name" },
    onFail = requireNotNull(onFailNode) { "onFail node not set for RuleSetNode $name" },
    contextUpdater = requireNotNull(contextUpdater) { "contextUpdater required for RuleSetNode $name (set a failure action)" },
  )
}
