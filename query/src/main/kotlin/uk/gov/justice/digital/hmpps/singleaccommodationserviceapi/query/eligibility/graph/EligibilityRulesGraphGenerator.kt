package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider

object EligibilityRulesGraphGenerator {

  fun generate(
    providers: Collection<EligibilityTreeProvider>,
    render: RulesGraphRenderer,
  ): GenerationResult {
    val graphs = providers
      .map { RulesGraphWalker().walk(it) }
      .sortedBy { it.treeName }
    val text = render.render(graphs)
    return GenerationResult(text)
  }
}
