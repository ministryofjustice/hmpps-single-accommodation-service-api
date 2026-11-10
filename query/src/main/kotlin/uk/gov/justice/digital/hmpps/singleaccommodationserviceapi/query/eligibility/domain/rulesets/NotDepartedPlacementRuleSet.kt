package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DepartedPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class NotDepartedPlacementRuleSet(
  departedPlacement: DepartedPlacementRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    departedPlacement,
  )

  override val name: String = "PASS if placement is not departed"

  override fun getRules(): List<Rule> = rules
}
