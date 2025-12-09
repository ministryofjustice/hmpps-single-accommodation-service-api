package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain

interface RuleSet {
  fun getRules(): List<Rule>
}
