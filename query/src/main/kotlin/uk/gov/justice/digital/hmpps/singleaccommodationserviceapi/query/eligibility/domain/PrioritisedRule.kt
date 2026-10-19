package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

data class PrioritisedRule(
  val rule: Rule,

  // default is -1, means it should never propagate the failure reason
  val priority: Int = -1,
) {
  val description: String
    get() = rule.description

  fun evaluate(data: DomainData): PrioritisedRuleResult = PrioritisedRuleResult(
    ruleResult = rule.evaluate(data),
    priority = priority,
  )
}
