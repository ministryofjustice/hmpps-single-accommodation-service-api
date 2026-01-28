package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

class RuleSetNodeBuilder(
  private val name: String,
  private val ruleSet: RuleSet,
  private val contextUpdater: ContextUpdater,
  private val engine: RulesEngine,
) {
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

  fun build(): RuleSetNode {
    return RuleSetNode(
      ruleSet = ruleSet,
      engine = engine,
      onPass = requireNotNull(onPassNode) {"onPass node not set for RuleSetNode $name" },
      onFail = requireNotNull(onFailNode) {"onFail node not set for RuleSetNode $name" },
      contextUpdater = contextUpdater,
    )
  }
}