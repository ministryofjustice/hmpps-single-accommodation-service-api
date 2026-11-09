package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NoArrivedPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class ArrivedPlacementRuleSet(
  noArrivedPlacement: NoArrivedPlacementRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    noArrivedPlacement,
  )

  override val name: String = "PASS if person has arrived at their placement"

  override fun getRules(): List<Rule> = rules
}
