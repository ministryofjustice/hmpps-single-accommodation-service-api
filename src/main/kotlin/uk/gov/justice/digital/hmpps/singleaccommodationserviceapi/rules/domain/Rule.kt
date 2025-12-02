package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain

interface Rule {
  val description: String
  val isGuidance: Boolean get() = false
  fun evaluate(data: DomainData): RuleResult
}
