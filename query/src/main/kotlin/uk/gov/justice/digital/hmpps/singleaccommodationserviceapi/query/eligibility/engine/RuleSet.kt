package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

interface RuleSet {
  /**
   * Human-readable label
   */
  val name: String

  fun getRules(): List<Rule>
}
