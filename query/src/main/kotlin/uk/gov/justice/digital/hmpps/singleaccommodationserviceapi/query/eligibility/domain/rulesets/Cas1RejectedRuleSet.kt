package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ReferralNotRejectedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1RejectedRuleSet(
  cas1NotRejected: Cas1ReferralNotRejectedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas1NotRejected,
  )

  override val name: String = "CAS1 Rejected Referral"

  override fun getRules(): List<Rule> = rules
}
