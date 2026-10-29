package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.rules.Cas2NotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.rules.Cas2NotSubmittedRule

@Component
class Cas2RuleSet(
  applicationSubmitted: Cas2NotSubmittedRule,
  suitableStatus: Cas2NotActiveRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(applicationSubmitted), PrioritisedRule(suitableStatus))
  override val name: String = "CAS2 Referral"
  override fun getRules(): List<PrioritisedRule> = rules
}
