package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CancelledPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas1ApplicationData

@Component
class NotArrivedPlacementRuleSet(
  cancelledPlacement: CancelledPlacementRule,
) : RuleSet<Cas1ApplicationData> {
  private val rules: List<Rule<Cas1ApplicationData>> = listOf(
    cancelledPlacement,
  )

  override val name: String = "PASS if placement is not arrived"

  override fun getRules(): List<Rule<Cas1ApplicationData>> = rules
}
