package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1NotRejectedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1RejectedRuleSet(
  cas1NotRejected: Cas1NotRejectedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas1NotRejected,
  )

  override val name: String = "PASS if person has a rejected referral"

  override fun getRules(): List<Rule> = rules
}
