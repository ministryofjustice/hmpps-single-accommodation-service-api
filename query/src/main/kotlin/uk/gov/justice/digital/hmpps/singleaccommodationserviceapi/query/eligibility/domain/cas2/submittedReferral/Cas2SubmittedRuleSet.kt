package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.submittedReferral

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.submittedReferral.rules.Cas2NotSubmittedRule

@Component
class Cas2SubmittedRuleSet(
  cas2NotSubmitted: Cas2NotSubmittedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas2NotSubmitted)
  override val name: String = "CAS2 Submitted Referral"
  override fun getRules(): List<Rule> = rules
}
