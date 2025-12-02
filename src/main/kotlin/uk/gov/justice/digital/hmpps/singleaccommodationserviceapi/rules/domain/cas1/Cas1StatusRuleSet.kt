package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.Cas1ApplicationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.Cas1PlacementRule

class Cas1StatusRuleSet : RuleSet {
  override fun getRules(): List<Rule> = listOf(
    Cas1ApplicationRule(),
    Cas1PlacementRule(),
  )
}
