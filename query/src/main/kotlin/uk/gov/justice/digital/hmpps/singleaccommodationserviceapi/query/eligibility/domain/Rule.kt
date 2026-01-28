package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

interface Rule {
  val description: String
  fun evaluate(data: DomainData): RuleResult
}
