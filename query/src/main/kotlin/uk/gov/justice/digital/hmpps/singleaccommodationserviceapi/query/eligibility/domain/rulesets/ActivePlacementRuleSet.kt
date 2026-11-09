package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DepartedPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class ActivePlacementRuleSet(
  departedPlacement: DepartedPlacementRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    departedPlacement,
  )

  override val name: String = "PASS if person has an active placement"

  override fun getRules(): List<Rule> = rules
}
