package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ReferralActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3ActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas1AndCas3ReferralData

@Component
class NoActiveCas1OrCas3ReferralsRuleSet(
  cas1Active: Cas1ReferralActiveRule,
  cas3Active: Cas3ActiveRule,
) : RuleSet<Cas1AndCas3ReferralData> {
  private val rules: List<Rule<Cas1AndCas3ReferralData>> = listOf(cas1Active, cas3Active)
  override val name: String = "No Active CAS1 Or CAS3 Referrals"
  override fun getRules(): List<Rule<Cas1AndCas3ReferralData>> = rules
}
