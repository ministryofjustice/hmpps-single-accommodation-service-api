package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.FemaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.MaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.WithinSixMonthsOfReleaseRule

class Cas1RuleSet : RuleSet {
  override fun getRules(): List<Rule> = listOf(
    STierRule(),
    MaleRiskRule(),
    FemaleRiskRule(),
    WithinSixMonthsOfReleaseRule(),
  )
}
