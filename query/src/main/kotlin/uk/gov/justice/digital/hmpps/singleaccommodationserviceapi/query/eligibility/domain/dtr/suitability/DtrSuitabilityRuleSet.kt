package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.DtrExpiredReferralRule

@Component
class DtrSuitabilityRuleSet(
  dtrStatus: DtrStatusRule,
  dtrPresent: DtrPresentRule,
  dtrExpiredReferral: DtrExpiredReferralRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(dtrStatus, dtrPresent, dtrExpiredReferral)
  override fun getRules(): List<Rule> = rules
}
