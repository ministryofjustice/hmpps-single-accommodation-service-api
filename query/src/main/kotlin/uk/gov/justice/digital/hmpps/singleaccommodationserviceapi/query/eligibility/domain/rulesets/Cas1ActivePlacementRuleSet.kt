package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1PlacementNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1ActivePlacementRuleSet(
  placementNotActive: Cas1PlacementNotActiveRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    placementNotActive,
  )

  override val name: String = "PASS if the CAS1 placement status is 'Arrived' or 'Upcoming'"

  override fun getRules(): List<Rule> = rules
}
