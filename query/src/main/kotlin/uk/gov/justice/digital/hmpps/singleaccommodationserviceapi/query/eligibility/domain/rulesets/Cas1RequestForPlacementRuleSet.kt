package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1NotRequestForPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1RequestForPlacementRuleSet(
  noRequestForPlacement: Cas1NotRequestForPlacementRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    noRequestForPlacement,
  )

  override val name: String = "Request For Placement"

  override fun getRules(): List<Rule> = rules
}
