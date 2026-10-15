package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.OutcomeNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetNode
import kotlin.reflect.KClass

private const val QUERY_MAIN_KOTLIN = "../query/src/main/kotlin"

object RulesGraphWalker {

  fun walk(provider: EligibilityTreeProvider): RulesGraph = walk(treeName(provider), provider.tree())

  fun walk(treeName: String, root: DecisionNode): RulesGraph {
    // Map of nodes converted to Graph items
    val nodes = LinkedHashMap<DecisionNode, GraphNode>()
    // Map of node ids
    val usedIds = mutableMapOf<String, DecisionNode>()
    // Node Edges
    val edges = mutableListOf<GraphEdge>()
    // record the nodes we are currently visiting
    val visitingStack = mutableSetOf<DecisionNode>()

    fun visit(node: DecisionNode) {
      if (node in visitingStack || node in nodes) return

      visitingStack += node
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
                className = rule::class.simpleName
                  ?: error("Rules should not be defined as anonymous classes"),
                description = rule.description,
                sourcePath = sourceLinkPath(rule::class),
              )
            },
            contextUpdater = contextUpdaterInfo(node.contextUpdater),
            ruleSet = node.ruleSet,
          )
          nodes[node] = graphNode
          visit(node.onPass)
          visit(node.onFail)
          edges += GraphEdge(graphNode.id, nodes.getValue(node.onPass).id, "PASS")
          edges += GraphEdge(graphNode.id, nodes.getValue(node.onFail).id, "FAIL")
        }
      }
      visitingStack -= node
    }

    visit(root)
    return RulesGraph(treeName, nodes.values.toList(), edges)
  }

  fun treeName(provider: EligibilityTreeProvider): String = provider::class.simpleName
    ?.removeSuffix("EligibilityTreeProvider")
    ?.uppercase()
    ?: provider.javaClass.simpleName
}

private fun uniqueId(base: String, node: DecisionNode, used: MutableMap<String, DecisionNode>): String {
  val existing = used[base]
  require(existing == null || existing === node) {
    "Duplicate graph node id '$base'. Give RuleSet and outcomes unique names."
  }
  used[base] = node
  return base
}

internal fun slug(raw: String): String {
  val cleaned = raw.replace(Regex("[^A-Za-z0-9_]"), "_")
  return if (cleaned.firstOrNull()?.isLetter() == true) cleaned else "n_$cleaned"
}

internal fun contextUpdaterInfo(updater: ContextUpdater): ContextUpdaterInfo? {
  val kClass = updater::class
  val simple = kClass.simpleName
  if (simple.isNullOrBlank()) {
    if (updater.propagatesFailureReasons) return null
    return ContextUpdaterInfo(name = "constant", description = updater.description, outcomes = updater.outcomes)
  }
  return ContextUpdaterInfo(
    name = simple,
    description = updater.description,
    outcomes = updater.outcomes,
    sourcePath = sourceLinkPath(kClass),
  )
}

internal fun sourceLinkPath(kClass: KClass<*>): String? {
  val javaClass = kClass.java
  if (javaClass.isAnonymousClass || javaClass.isLocalClass || javaClass.enclosingClass != null) return null
  val packageName = javaClass.packageName
  val simpleName = kClass.simpleName ?: return null
  if (packageName.isBlank()) return null
  return "$QUERY_MAIN_KOTLIN/${packageName.replace('.', '/')}/$simpleName.kt"
}
