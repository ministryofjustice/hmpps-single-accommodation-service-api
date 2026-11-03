package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligible

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligible.rules.Cas1ActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligible.rules.Cas3ActiveRule

@Component
class NoActiveCas1OrCas3ReferralsRuleSet(
  cas1Active: Cas1ActiveRule,
  cas3Active: Cas3ActiveRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas1Active, cas3Active)
  override val name: String = "No Active CAS1 Or CAS3 Referrals"
  override fun getRules(): List<Rule> = rules
}
