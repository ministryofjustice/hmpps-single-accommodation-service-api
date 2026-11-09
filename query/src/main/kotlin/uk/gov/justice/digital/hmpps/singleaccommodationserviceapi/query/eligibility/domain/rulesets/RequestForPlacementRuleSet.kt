package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NoRequestForPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class RequestForPlacementRuleSet(
  noRequestForPlacement: NoRequestForPlacementRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    noRequestForPlacement,
  )

  override val name: String = "PASS if person has a request for placement"

  override fun getRules(): List<Rule> = rules
}
