package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CrsNotLiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DtrExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class DtrNotExpiredAndCrsLiveRuleSet(
  dtrExpired: DtrExpiredRule,
  crsNotLive: CrsNotLiveRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(dtrExpired, crsNotLive)
  override val name: String = "PASS if the DTR was submitted in the last 26 weeks and there is a live CRS referral"
  override fun getRules(): List<Rule> = rules
}
