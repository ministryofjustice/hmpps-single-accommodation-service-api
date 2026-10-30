package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.rules.Cas1ExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.rules.Cas1NotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.rules.Cas1NotPresentRule

@Component
class Cas1RuleSet(
  cas1NotPresent: Cas1NotPresentRule,
  cas1NotActive: Cas1NotActiveRule,
  cas1Expired: Cas1ExpiredRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(cas1NotPresent),
    PrioritisedRule(cas1Expired),
    PrioritisedRule(cas1NotActive),
  )

  override val name: String = "CAS1 Referral"

  override fun getRules(): List<PrioritisedRule> = rules
}
