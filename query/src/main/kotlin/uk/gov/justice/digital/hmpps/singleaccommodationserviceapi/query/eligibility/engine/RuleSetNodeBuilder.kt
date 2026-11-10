package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew

class RuleSetNodeBuilder<D>(
  private var name: String,
  private val ruleSet: RuleSet<D>,
  private val engine: RulesEngine,
) {
  private var onFailContextUpdater: ContextUpdater<D>? = null
  private var onPassContextUpdater: ContextUpdater<D>? = null
  private var onPassNode: DecisionNode<D>? = null
  private var onFailNode: DecisionNode<D>? = null

  fun onPass(node: DecisionNode<D>): RuleSetNodeBuilder<D> {
    onPassNode = node
    return this
  }

  fun onFail(node: DecisionNode<D>): RuleSetNodeBuilder<D> {
    onFailNode = node
    return this
  }

  fun applyContextUpdaterOnPass(updater: ContextUpdater<D>): RuleSetNodeBuilder<D> {
    onPassContextUpdater = updater
    return this
  }

  fun applyContextUpdaterOnFailure(updater: ContextUpdater<D>): RuleSetNodeBuilder<D> {
    onFailContextUpdater = updater
    return this
  }

  fun propagateContextOnFailure(): RuleSetNodeBuilder<D> {
    onFailContextUpdater = ContextUpdater.identity()
    return this
  }

  fun setConstantServiceResultOnFailure(result: ServiceResultNew): RuleSetNodeBuilder<D> {
    onFailContextUpdater = ContextUpdater.constant(result)
    return this
  }

  fun onPassAndFail(node: DecisionNode<D>): RuleSetNodeBuilder<D> = onPass(node).onFail(node)

  fun build(): RuleSetNode<D> = RuleSetNode(
    ruleSetName = name,
    ruleSet = ruleSet,
    engine = engine,
    onPass = requireNotNull(onPassNode) { "onPass node not set for RuleSetNode $name" },
    onPassContextUpdater = onPassContextUpdater,
    onFail = requireNotNull(onFailNode) { "onFail node not set for RuleSetNode $name" },
    onFailContextUpdater = onFailContextUpdater,
  )
}
