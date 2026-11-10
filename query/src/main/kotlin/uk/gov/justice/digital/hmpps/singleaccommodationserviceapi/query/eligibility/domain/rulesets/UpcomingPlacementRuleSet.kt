package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.ArrivedPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas1ApplicationData

@Component
class UpcomingPlacementRuleSet(
  arrivedPlacement: ArrivedPlacementRule,
) : RuleSet<Cas1ApplicationData> {
  private val rules: List<Rule<Cas1ApplicationData>> = listOf(
    arrivedPlacement,
  )

  override val name: String = "PASS if placement is booked"

  override fun getRules(): List<Rule<Cas1ApplicationData>> = rules
}
