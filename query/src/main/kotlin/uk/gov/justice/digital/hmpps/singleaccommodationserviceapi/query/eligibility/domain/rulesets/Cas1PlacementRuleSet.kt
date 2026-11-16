package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1NoPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1PlacementRuleSet(
  noPlacement: Cas1NoPlacementRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    noPlacement,
  )

  override val name: String = "PASS if there is a CAS1 placement"

  override fun getRules(): List<Rule> = rules
}
