package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.placement

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.placement.rules.NoPlacementRule

@Component
class PlacementRuleSet(
  noPlacement: NoPlacementRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    noPlacement,
  )

  override val name: String = "Placement"

  override fun getRules(): List<Rule> = rules
}
