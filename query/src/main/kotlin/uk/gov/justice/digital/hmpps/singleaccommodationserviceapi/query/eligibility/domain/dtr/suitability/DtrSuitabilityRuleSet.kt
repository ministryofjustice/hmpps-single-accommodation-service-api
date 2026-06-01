package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.DtrExpiredReferralRule

@Component
class DtrSuitabilityRuleSet(
  dtrPresent: DtrPresentRule,
  dtrExpiredReferral: DtrExpiredReferralRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(dtrPresent, dtrExpiredReferral)
  override fun getRules(): List<Rule> = rules
}
