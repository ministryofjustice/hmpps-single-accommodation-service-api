package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

enum class GraphNodeKind {
  RULE_SET,
  OUTCOME,
}

data class RuleInfo(
  val className: String,
  val description: String,
)

data class ContextUpdaterInfo(
  val name: String,
  val description: String,
  val outcomes: Map<String, ServiceResult> = emptyMap(),
)

data class GraphNode(
  val id: String,
  val title: String,
  val kind: GraphNodeKind,
  val rules: List<RuleInfo> = emptyList(),
  val ruleSet: RuleSet? = null,
  val contextUpdater: ContextUpdaterInfo? = null,
)

data class GraphEdge(
  val from: String,
  val to: String,
  val label: String,
)

data class RulesGraph(
  val treeName: String,
  val nodes: List<GraphNode>,
  val edges: List<GraphEdge>,
)

data class GenerationResult(
  val text: String,
)
