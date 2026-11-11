package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ArrivedPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1UpcomingPlacementRuleSet(
  arrivedPlacement: Cas1ArrivedPlacementRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    arrivedPlacement,
  )

  override val name: String = "PASS if placement is booked"

  override fun getRules(): List<Rule> = rules
}
