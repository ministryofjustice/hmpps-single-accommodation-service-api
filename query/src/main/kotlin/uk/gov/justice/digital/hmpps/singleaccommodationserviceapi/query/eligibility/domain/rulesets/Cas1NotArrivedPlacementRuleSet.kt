package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1CancelledPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1NotArrivedPlacementRuleSet(
  cancelledPlacement: Cas1CancelledPlacementRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cancelledPlacement,
  )

  override val name: String = "PASS if placement is not arrived"

  override fun getRules(): List<Rule> = rules
}
