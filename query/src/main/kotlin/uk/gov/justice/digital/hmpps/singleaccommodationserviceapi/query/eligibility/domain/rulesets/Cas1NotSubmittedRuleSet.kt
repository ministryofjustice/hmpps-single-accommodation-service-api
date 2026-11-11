package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ReferralSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1NotSubmittedRuleSet(
  cas1Submitted: Cas1ReferralSubmittedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas1Submitted,
  )

  override val name: String = "CAS1 Not Submitted Referral"

  override fun getRules(): List<Rule> = rules
}
