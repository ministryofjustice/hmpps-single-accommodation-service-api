package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ReferralNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1ActiveRuleSet(
  cas1NotActive: Cas1ReferralNotActiveRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas1NotActive,
  )

  override val name: String = "CAS1 Active Referral"

  override fun getRules(): List<Rule> = rules
}
