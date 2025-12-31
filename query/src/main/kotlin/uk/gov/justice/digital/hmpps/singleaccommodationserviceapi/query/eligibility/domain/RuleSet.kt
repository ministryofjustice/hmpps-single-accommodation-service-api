package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

interface RuleSet {
  fun getRules(): List<Rule>
}
