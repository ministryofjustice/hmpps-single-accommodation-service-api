package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.DtrExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.rules.DtrNotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.rules.DtrWithdrawnRule

@Component
class DtrRuleSet(
  dtrPresent: DtrNotPresentRule,
  dtrNotWithdrawn: DtrWithdrawnRule,
  dtrExpiredReferral: DtrExpiredRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(dtrPresent), PrioritisedRule(dtrNotWithdrawn), PrioritisedRule(dtrExpiredReferral))
  override val name: String = "Suitability"
  override fun getRules(): List<PrioritisedRule> = rules
}
