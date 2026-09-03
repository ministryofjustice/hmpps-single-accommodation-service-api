package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.OutcomeNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetNode

object RulesGraphWalker {

  fun walk(provider: EligibilityTreeProvider): RulesGraph = walk(treeName(provider), provider.tree())

  fun walk(treeName: String, root: DecisionNode): RulesGraph {
    // Map of nodes converted to Graph items
    val nodes = LinkedHashMap<DecisionNode, GraphNode>()
    // Map of node ids
    val usedIds = mutableMapOf<String, DecisionNode>()

    fun visit(node: DecisionNode) {
      when (node) {
        is OutcomeNode -> {
          nodes[node] = GraphNode(
            id = uniqueId(node.name, node, usedIds),
            title = node.name,
            kind = GraphNodeKind.OUTCOME,
          )
        }
        is RuleSetNode -> {
          nodes[node] = GraphNode(
            id = uniqueId(node.ruleSetName, node, usedIds),
            title = node.ruleSetName,
            kind = GraphNodeKind.RULE_SET,
            rules = node.ruleSet.getRules().map { rule ->
              RuleInfo(
                className = rule::class.simpleName ?: "Rule",
                description = rule.description,
              )
            },
          )
          visit(node.onPass)
          visit(node.onFail)
        }
      }
    }

    visit(root)
    return RulesGraph(treeName, nodes.values.toList())
  }

  fun treeName(provider: EligibilityTreeProvider): String = provider::class.simpleName
    ?.removeSuffix("EligibilityTreeProvider")
    ?.uppercase()
    ?: provider.javaClass.simpleName
}

private fun uniqueId(base: String, node: DecisionNode, used: MutableMap<String, DecisionNode>): String {
  val existing = used[base]
  if (existing == null || existing === node) {
    used[base] = node
    return base
  }
  val suffixed = "${base}_${System.identityHashCode(node)}"
  used[suffixed] = node
  return suffixed
}
