package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NoPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class PlacementRuleSet(
  noPlacement: NoPlacementRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    noPlacement,
  )

  override val name: String = "PASS if person has a placement"

  override fun getRules(): List<Rule> = rules
}
