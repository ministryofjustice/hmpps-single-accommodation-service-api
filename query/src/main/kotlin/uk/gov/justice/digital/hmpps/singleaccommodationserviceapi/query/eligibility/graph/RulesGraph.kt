package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

enum class GraphNodeKind {
  RULE_SET,
  OUTCOME,
}

data class RuleInfo(
  val className: String,
  val description: String,
)

data class GraphNode(
  val id: String,
  val title: String,
  val kind: GraphNodeKind,
  val rules: List<RuleInfo> = emptyList(),
  val ruleSet: RuleSet? = null,
)

data class RulesGraph(
  val treeName: String,
  val nodes: List<GraphNode>,
)
