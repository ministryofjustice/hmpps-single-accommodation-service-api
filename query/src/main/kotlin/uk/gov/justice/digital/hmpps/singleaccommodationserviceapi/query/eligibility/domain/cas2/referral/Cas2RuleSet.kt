package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.rules.Cas2NotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.rules.Cas2NotSubmittedRule

@Component
class Cas2RuleSet(
  cas2NotSubmitted: Cas2NotSubmittedRule,
  cas2NotActive: Cas2NotActiveRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas2NotSubmitted, cas2NotActive)
  override val name: String = "CAS2 Referral"
  override fun getRules(): List<Rule> = rules
}
