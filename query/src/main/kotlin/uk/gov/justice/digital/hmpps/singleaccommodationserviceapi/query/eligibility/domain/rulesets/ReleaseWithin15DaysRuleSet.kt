package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.ReleaseOutside15DaysRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class ReleaseWithin15DaysRuleSet(
  releaseOutside15Days: ReleaseOutside15DaysRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(releaseOutside15Days)
  override val name: String = "PASS if person can start referral"
  override fun getRules(): List<Rule> = rules
}
