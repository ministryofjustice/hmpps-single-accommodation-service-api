package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain

interface Rule {
  val description: String
  val actionable: Boolean get() = false
  fun evaluate(data: DomainData): RuleResult
}
