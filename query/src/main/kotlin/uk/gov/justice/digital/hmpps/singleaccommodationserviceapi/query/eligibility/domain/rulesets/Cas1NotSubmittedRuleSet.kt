package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1SubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1NotSubmittedRuleSet(
  cas1Submitted: Cas1SubmittedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas1Submitted,
  )

  override val name: String = "PASS if person has a draft referral"

  override fun getRules(): List<Rule> = rules
}
