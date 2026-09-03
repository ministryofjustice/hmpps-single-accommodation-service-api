package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider

object EligibilityRulesGraphGenerator {

  fun generate(
    providers: Collection<EligibilityTreeProvider>,
  ): GenerationResult {
    val graphs = providers
      .map { RulesGraphWalker.walk(it) }
      .sortedBy { it.treeName }

    // Possibly Validate graphs for rendering??

    val text = RulesGraphMarkdownRenderer.render(graphs)
    return GenerationResult(text)
  }
}
