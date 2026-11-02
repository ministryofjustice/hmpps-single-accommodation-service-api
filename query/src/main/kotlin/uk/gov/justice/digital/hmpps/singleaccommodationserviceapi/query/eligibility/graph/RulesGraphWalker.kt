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
    visit(root)
    return RulesGraph(treeName, nodes.values.toList(), edges)
  }

  fun visit(node: DecisionNode) {
    val visitingStack = mutableSetOf<DecisionNode>()

    if (node in visitingStack || node in nodes) return

    visitingStack += node
    when (node) {
      is OutcomeNode -> {
        nodes[node] = node.toGraphNode()
      }

      is RuleSetNode -> {
        val graphNode = node.toGraphNode()
        nodes[node] = graphNode

        addOutcomeEdge(
          fromGraphNode = graphNode,
          toNode = node.onPass,
          contextUpdater = node.onPassContextUpdater,
          edgeLabel = "PASS",
        )

        addOutcomeEdge(
          fromGraphNode = graphNode,
          toNode = node.onFail,
          contextUpdater = node.onFailContextUpdater,
          edgeLabel = "FAIL",
        )
      }
    }
    visitingStack -= node
  }

  private fun addOutcomeEdge(
    fromGraphNode: GraphNode,
    toNode: DecisionNode,
    contextUpdater: ContextUpdater?,
    edgeLabel: String,
  ) {
    val onPassSourceNode = if (contextUpdater == null || contextUpdater === ContextUpdater.identity()) {
      fromGraphNode
    } else {
      val contextUpdaterNode = if (contextUpdater.terminalNode) contextUpdater.toOutcomeNode() else contextUpdater.toContextUpdaterNode()

      nodes[contextUpdater] = contextUpdaterNode

      edges += GraphEdge(from = fromGraphNode.id, to = contextUpdaterNode.id, label = edgeLabel)

      if (contextUpdater.terminalNode) return

      contextUpdaterNode
    }

    visit(toNode)
    edges += GraphEdge(onPassSourceNode.id, nodes.getValue(toNode).id, edgeLabel)
  }

  private fun OutcomeNode.toGraphNode() = GraphNode(
    id = ensureUniqueId(slug(key), this),
    title = name,
    kind = GraphNodeKind.OUTCOME,
  )

  private fun RuleSetNode.toGraphNode() = GraphNode(
    id = ensureUniqueId(slug(ruleSetName), this),
    title = ruleSetName,
    kind = GraphNodeKind.RULE_SET,
    rules = ruleSet.getRules().map { priorityRule ->
      RuleInfo(
        className = priorityRule.rule::class.simpleName
          ?: error("Rules should not be defined as anonymous classes"),
        description = priorityRule.rule.description,
        sourcePath = sourceLinkPath(priorityRule.rule::class),
      )
    },
    onPassContextUpdater = contextUpdaterInfo(onPassContextUpdater, ContextUpdaterKind.ON_PASS),
    onFailContextUpdater = contextUpdaterInfo(onFailContextUpdater, ContextUpdaterKind.ON_FAIL),
    ruleSet = ruleSet,
  )

  private fun ContextUpdater.toOutcomeNode() = GraphNode(
    id = ensureUniqueId("outcome_" + slug(graphKey()), this),
    title = description,
    kind = GraphNodeKind.OUTCOME,
  )

  private fun ContextUpdater.toContextUpdaterNode() = GraphNode(
    id = ensureUniqueId("updater_" + slug(graphKey()), this),
    title = description,
    kind = GraphNodeKind.CONTEXT_UPDATER,
    onPassContextUpdater = null,
    onFailContextUpdater = null,
  )

  // Ids come from the class name, or for anonymous (e.g. constant) updaters the status enum names they set,
  // never from the description, because distinct statuses can share a UI label.
  private fun ContextUpdater.graphKey(): String = this::class.simpleName
    ?: outcomes.values.map { it.serviceStatus.name }.distinct().joinToString("_")
      .let { if (it.isEmpty()) "anonymous" else "constant_$it" }

  fun treeName(provider: EligibilityTreeProvider): String = provider::class.simpleName
    ?.removeSuffix("EligibilityTreeProvider")
    ?.uppercase()
    ?: provider.javaClass.simpleName

  private fun ensureUniqueId(base: String, node: Any): String {
    val existing = usedNodeIds[base]
    require(existing == null || existing === node) {
      "Duplicate graph node id '$base'. Give RuleSets unique names, and outcomes and context updaters unique keys."
    }
    usedNodeIds[base] = node
    return base
  }
}

internal fun slug(raw: String): String {
  val cleaned = raw.replace(Regex("[^A-Za-z0-9_]"), "_")
  return if (cleaned.firstOrNull()?.isLetter() == true) cleaned else "n_$cleaned"
}

internal fun contextUpdaterInfo(
  updater: ContextUpdater?,
  updaterKind: ContextUpdaterKind,
): ContextUpdaterInfo? {
  if (updater == null) return null
  val kClass = updater::class
  val simple = kClass.simpleName
  if (simple.isNullOrBlank()) {
    if (updater.propagatesFailureReasons) return null
    return ContextUpdaterInfo(name = "constant", description = updater.description, outcomes = updater.outcomes, kind = updaterKind)
  }
  return ContextUpdaterInfo(
    name = simple,
    description = updater.description,
    outcomes = updater.outcomes,
    sourcePath = sourceLinkPath(kClass),
    kind = updaterKind,
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
