package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DepartedPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas1ApplicationData

@Component
class NotDepartedPlacementRuleSet(
  departedPlacement: DepartedPlacementRule,
) : RuleSet<Cas1ApplicationData> {
  private val rules: List<Rule<Cas1ApplicationData>> = listOf(
    departedPlacement,
  )

  override val name: String = "PASS if placement is not departed"

  override fun getRules(): List<Rule<Cas1ApplicationData>> = rules
}
