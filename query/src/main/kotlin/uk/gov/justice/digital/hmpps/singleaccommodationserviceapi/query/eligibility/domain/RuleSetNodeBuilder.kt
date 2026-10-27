package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

class RuleSetNodeBuilder(
  private var name: String,
  private val ruleSet: RuleSet,
  private val engine: RulesEngine,
) {
  private var onFailContextUpdater: ContextUpdater? = null
  private var onPassContextUpdater: ContextUpdater? = null
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

  fun applyContextUpdaterOnPass(updater: ContextUpdater): RuleSetNodeBuilder {
    onPassContextUpdater = updater
    return this
  }

  fun applyContextUpdaterOnFailure(updater: ContextUpdater): RuleSetNodeBuilder {
    onFailContextUpdater = updater
    return this
  }

  fun propagateContextOnFailure(): RuleSetNodeBuilder {
    onFailContextUpdater = ContextUpdater.identity()
    return this
  }

  fun setConstantServiceResultOnFailure(result: ServiceResultNew): RuleSetNodeBuilder {
    onFailContextUpdater = ContextUpdater.constant(result)
    return this
  }

  fun onPassAndFail(node: DecisionNode): RuleSetNodeBuilder = onPass(node).onFail(node)

  fun build(): RuleSetNode = RuleSetNode(
    ruleSetName = name,
    ruleSet = ruleSet,
    engine = engine,
    onPass = requireNotNull(onPassNode) { "onPass node not set for RuleSetNode $name" },
    onPassContextUpdater = onPassContextUpdater,
    onFail = requireNotNull(onFailNode) { "onFail node not set for RuleSetNode $name" },
    onFailContextUpdater = requireNotNull(onFailContextUpdater) { "contextUpdater required for RuleSetNode $name (set a failure action)" },
  )
}
