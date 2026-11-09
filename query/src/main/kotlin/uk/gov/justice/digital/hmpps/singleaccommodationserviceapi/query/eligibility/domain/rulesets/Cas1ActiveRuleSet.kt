package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1NotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1ActiveRuleSet(
  cas1NotActive: Cas1NotActiveRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas1NotActive,
  )

  override val name: String = "PASS if person has an active referral"

  override fun getRules(): List<Rule> = rules
}
