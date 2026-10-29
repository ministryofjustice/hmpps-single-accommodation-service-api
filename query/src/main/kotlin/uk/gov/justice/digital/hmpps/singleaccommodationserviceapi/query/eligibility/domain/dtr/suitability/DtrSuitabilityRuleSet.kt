package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.DtrExpiredReferralRule

@Component
class DtrSuitabilityRuleSet(
  dtrPresent: DtrPresentRule,
  dtrNotWithdrawn: DtrNotWithdrawnRule,
  dtrExpiredReferral: DtrExpiredReferralRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(dtrPresent), PrioritisedRule(dtrNotWithdrawn), PrioritisedRule(dtrExpiredReferral))
  override val name: String = "Suitability"
  override fun getRules(): List<PrioritisedRule> = rules
}
