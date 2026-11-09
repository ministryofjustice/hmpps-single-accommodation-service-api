package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1NotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1NotExpiredRuleSet(
  cas1Expired: Cas1ExpiredRule,
  cas1IsPresent: Cas1NotPresentRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas1Expired,
    cas1IsPresent,
  )

  override val name: String = "PASS if referral has not expired"

  override fun getRules(): List<Rule> = rules
}
