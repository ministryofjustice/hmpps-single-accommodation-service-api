package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

interface RuleSet<in D> {
  /**
   * Human-readable label
   */
  val name: String

  fun getRules(): List<Rule<D>>
}
