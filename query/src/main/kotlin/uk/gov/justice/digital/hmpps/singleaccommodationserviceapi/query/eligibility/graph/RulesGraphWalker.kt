package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.OutcomeNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetNode
import kotlin.reflect.KClass

private const val QUERY_MAIN_KOTLIN = "../query/src/main/kotlin"

/**
 * Produces a RulesGraph (nodes and edges) for a given EligibilityTreeProvider
 */
class RulesGraphWalker {

  val nodes = LinkedHashMap<Any, GraphNode>()
  val usedNodeIds = mutableMapOf<String, Any>()
  val edges = mutableListOf<GraphEdge>()

  fun walk(provider: EligibilityTreeProvider): RulesGraph = walk(treeName(provider), provider.tree())

  fun walk(treeName: String, root: DecisionNode): RulesGraph {
    // record the nodes we are currently visiting
    val visitingStack = mutableSetOf<DecisionNode>()

    fun visit(node: DecisionNode) {
      if (node in visitingStack || node in nodes) return

      visitingStack += node
      when (node) {
        is OutcomeNode -> {
          nodes[node] = node.toGraphNode()
        }

        is RuleSetNode -> {
          val graphNode = node.toGraphNode()
          nodes[node] = graphNode

          visit(node.onPass)
          edges += GraphEdge(graphNode.id, nodes.getValue(node.onPass).id, "PASS")

          val onFailSourceNode = if (node.contextUpdater === ContextUpdater.identity()) {
            graphNode
          } else {
            node.contextUpdater.let { contextUpdater ->
              val contextUpdaterNode = contextUpdater.toGraphNode()

              nodes[contextUpdater] = contextUpdaterNode

              edges += GraphEdge(from = graphNode.id, to = contextUpdaterNode.id, label = "FAIL")

              contextUpdaterNode
            }
          }

          visit(node.onFail)
          edges += GraphEdge(onFailSourceNode.id, nodes.getValue(node.onFail).id, "FAIL")
        }
      }
      visitingStack -= node
    }

    visit(root)
    return RulesGraph(treeName, nodes.values.toList(), edges)
  }

  private fun OutcomeNode.toGraphNode() = GraphNode(
    id = ensureUniqueId(slug(name), this),
    title = name,
    kind = GraphNodeKind.OUTCOME,
  )

  private fun RuleSetNode.toGraphNode() = GraphNode(
    id = ensureUniqueId(slug(ruleSetName), this),
    title = ruleSet.name?.takeIf { it.isNotBlank() } ?: ruleSetName,
    kind = GraphNodeKind.RULE_SET,
    rules = ruleSet.getRules().map { priorityRule ->
      RuleInfo(
        className = priorityRule.rule::class.simpleName
          ?: error("Rules should not be defined as anonymous classes"),
        description = priorityRule.rule.description,
        sourcePath = sourceLinkPath(priorityRule.rule::class),
      )
    },
    contextUpdater = contextUpdaterInfo(contextUpdater),
    ruleSet = ruleSet,
  )

  private fun ContextUpdater.toGraphNode() = GraphNode(
    id = ensureUniqueId(slug(description), this),
    title = description,
    kind = GraphNodeKind.CONTEXT_UPDATER,
    contextUpdater = null,
  )

  fun treeName(provider: EligibilityTreeProvider): String = provider::class.simpleName
    ?.removeSuffix("EligibilityTreeProvider")
    ?.uppercase()
    ?: provider.javaClass.simpleName

  private fun ensureUniqueId(base: String, node: Any): String {
    val existing = usedNodeIds[base]
    require(existing == null || existing === node) {
      "Duplicate graph node id '$base'. Give RuleSet and outcomes unique names."
    }
    usedNodeIds[base] = node
    return base
  }
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
