package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligible

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligible.rules.Cas1ActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligible.rules.Cas3ActiveRule

@Component
class NoActiveCas1OrCas3ReferralsRuleSet(
  cas1ApplicationNotSuitableRule: Cas1ActiveRule,
  cas3ActiveRule: Cas3ActiveRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(cas1ApplicationNotSuitableRule, 0), PrioritisedRule(cas3ActiveRule, 0))
  override val name: String = "No Active CAS1 Or CAS3 Referrals"
  override fun getRules(): List<PrioritisedRule> = rules
}
