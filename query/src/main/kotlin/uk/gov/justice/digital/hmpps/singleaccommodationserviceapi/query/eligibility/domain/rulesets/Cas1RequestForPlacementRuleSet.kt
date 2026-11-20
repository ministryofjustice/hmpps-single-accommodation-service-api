package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1NoRequestForPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1RequestForPlacementRuleSet(
  noRequestForPlacement: Cas1NoRequestForPlacementRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    noRequestForPlacement,
  )

  override val name: String = "PASS if there is a CAS1 request for placement status"

  override fun getRules(): List<Rule> = rules
}
