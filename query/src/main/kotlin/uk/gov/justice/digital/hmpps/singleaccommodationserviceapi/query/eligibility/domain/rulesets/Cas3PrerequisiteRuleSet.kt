package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CrsSubmittedRuleMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CrsSubmittedRuleNonMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DtrExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3PrerequisiteRuleSet(
  dtrExpired: DtrExpiredRule,
  crsSubmittedRuleMale: CrsSubmittedRuleMale,
  crsSubmittedRuleNonMale: CrsSubmittedRuleNonMale,
) : RuleSet {
  private val rules: List<Rule> = listOf(dtrExpired, crsSubmittedRuleMale, crsSubmittedRuleNonMale)
  override val name: String = "PASS if the DTR is in date and the CRS referral is live or completed"
  override fun getRules(): List<Rule> = rules
}
