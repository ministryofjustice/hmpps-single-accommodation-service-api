package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1NotSubmittedRequestRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1SubmittedRequestRuleSet(
  notSubmittedRequest: Cas1NotSubmittedRequestRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    notSubmittedRequest,
  )

  override val name: String = "PASS if referral is submitted or awaiting placement"

  override fun getRules(): List<Rule> = rules
}
