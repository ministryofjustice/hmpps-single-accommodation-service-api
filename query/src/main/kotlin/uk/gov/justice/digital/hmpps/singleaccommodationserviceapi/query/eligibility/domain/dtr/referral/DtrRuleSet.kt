package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.DtrExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.rules.DtrNotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.rules.DtrWithdrawnRule

@Component
class DtrRuleSet(
  dtrNotPresent: DtrNotPresentRule,
  dtrWithdrawn: DtrWithdrawnRule,
  dtrExpired: DtrExpiredRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(dtrNotPresent), PrioritisedRule(dtrWithdrawn), PrioritisedRule(dtrExpired))
  override val name: String = "DTR"
  override fun getRules(): List<PrioritisedRule> = rules
}
