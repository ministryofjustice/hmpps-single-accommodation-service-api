package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NoUpcomingPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class UpcomingPlacementRuleSet(
  noUpcomingPlacement: NoUpcomingPlacementRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    noUpcomingPlacement,
  )

  override val name: String = "PASS if person has a placement booked"

  override fun getRules(): List<Rule> = rules
}
