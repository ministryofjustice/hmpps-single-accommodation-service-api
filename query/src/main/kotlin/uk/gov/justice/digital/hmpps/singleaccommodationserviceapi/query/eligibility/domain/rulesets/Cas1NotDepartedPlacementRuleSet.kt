package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1DepartedPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1NotDepartedPlacementRuleSet(
  departedPlacement: Cas1DepartedPlacementRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    departedPlacement,
  )

  override val name: String = "PASS if placement is not departed"

  override fun getRules(): List<Rule> = rules
}
