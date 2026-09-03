package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider

object EligibilityRulesGraphGenerator {

  fun generate(
    providers: Collection<EligibilityTreeProvider>,
  ): List<RulesGraph> {
    val graphs = providers
      .map { RulesGraphWalker.walk(it) }
      .sortedBy { it.treeName }
    return graphs
  }
}
