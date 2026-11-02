package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

interface RuleSet {
  /**
   * Human-readable label
   */
  val name: String

  fun getRules(): List<PrioritisedRule>
}
