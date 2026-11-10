package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1NoPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas1ApplicationData

@Component
class PlacementRuleSet(
  noPlacement: Cas1NoPlacementRule,
) : RuleSet<Cas1ApplicationData> {
  private val rules: List<Rule<Cas1ApplicationData>> = listOf(
    noPlacement,
  )

  override val name: String = "Placement"

  override fun getRules(): List<Rule<Cas1ApplicationData>> = rules
}
