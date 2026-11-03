package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1NotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1RuleSet(
  cas1NotPresent: Cas1NotPresentRule,
  cas1Expired: Cas1ExpiredRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas1NotPresent,
    cas1Expired,
  )

  override val name: String = "CAS1 Referral"

  override fun getRules(): List<Rule> = rules
}
