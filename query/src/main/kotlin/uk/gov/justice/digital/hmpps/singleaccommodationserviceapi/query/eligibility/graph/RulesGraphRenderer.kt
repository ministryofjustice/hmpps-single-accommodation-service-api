package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

fun interface RulesGraphRenderer {
  fun render(graphs: List<RulesGraph>): String
}
