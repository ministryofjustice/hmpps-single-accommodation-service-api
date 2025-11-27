package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain

interface RuleSet {
  fun getRules(): List<Rule>
}
