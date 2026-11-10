package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DtrExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DtrNotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DtrWithdrawnRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DtrReferralData

@Component
class DtrRuleSet(
  dtrNotPresent: DtrNotPresentRule,
  dtrWithdrawn: DtrWithdrawnRule,
  dtrExpired: DtrExpiredRule,
) : RuleSet<DtrReferralData> {
  private val rules: List<Rule<DtrReferralData>> = listOf(dtrNotPresent, dtrWithdrawn, dtrExpired)
  override val name: String = "DTR"
  override fun getRules(): List<Rule<DtrReferralData>> = rules
}
