package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

data class PrioritisedRuleResult(
  val ruleResult: RuleResult,

  // default is -1, means it should never propagate the failure reason
  val priority: Int = -1,
)
