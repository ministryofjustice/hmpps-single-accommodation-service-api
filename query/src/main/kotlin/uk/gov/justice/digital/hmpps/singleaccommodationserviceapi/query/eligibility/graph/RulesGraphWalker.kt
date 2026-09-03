package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
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
    // Node Edges
    val edges = mutableListOf<GraphEdge>()
    // handles infinite cycles
    val cycles = mutableListOf<String>()
    // record the nodes we are currently visiting
    val visiting = mutableSetOf<DecisionNode>()

    fun visit(node: DecisionNode) {
      if (node in visiting) {
        cycles += "cycle involving ${nodeLabel(node)}"
        return
      }
      if (node in nodes) return

      visiting += node
      when (node) {
        is OutcomeNode -> {
          nodes[node] = GraphNode(
            id = uniqueId(slug(node.name), node, usedIds),
            title = node.name,
            kind = GraphNodeKind.OUTCOME,
          )
        }
        is RuleSetNode -> {
          val graphNode = GraphNode(
            id = uniqueId(slug(node.ruleSetName), node, usedIds),
            title = node.ruleSetName,
            kind = GraphNodeKind.RULE_SET,
            rules = node.ruleSet.getRules().map { rule ->
              RuleInfo(
                className = rule::class.simpleName ?: "Rule",
                description = rule.description,
              )
            },
            contextUpdater = contextUpdaterName(node.contextUpdater),
            ruleSet = node.ruleSet,
          )
          nodes[node] = graphNode
          visit(node.onPass)
          visit(node.onFail)
          val onPassId = nodes[node.onPass]?.id
          val onFailId = nodes[node.onFail]?.id
          if (onPassId != null) {
            edges += GraphEdge(graphNode.id, onPassId, "PASS")
          }
          if (onFailId != null) {
            edges += GraphEdge(graphNode.id, onFailId, "FAIL")
          }
        }
      }
      visiting -= node
    }

    visit(root)
    return RulesGraph(treeName, nodes.values.toList(), edges, cycles)
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

internal fun slug(raw: String): String {
  val cleaned = raw.replace(Regex("[^A-Za-z0-9_]"), "_")
  return if (cleaned.firstOrNull()?.isLetter() == true) cleaned else "n_$cleaned"
}

private fun nodeLabel(node: DecisionNode): String = when (node) {
  is OutcomeNode -> node.name
  is RuleSetNode -> node.ruleSetName
}

internal fun contextUpdaterName(updater: ContextUpdater): String {
  val simple = updater::class.simpleName
  if (!simple.isNullOrBlank()) return simple
  return if (updater.propagatesFailureReasons) "identity" else "constant"
}
