package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

interface Rule {
  val description: String
  val actionable: Boolean get() = false
  fun evaluate(data: DomainData): RuleResult
}
