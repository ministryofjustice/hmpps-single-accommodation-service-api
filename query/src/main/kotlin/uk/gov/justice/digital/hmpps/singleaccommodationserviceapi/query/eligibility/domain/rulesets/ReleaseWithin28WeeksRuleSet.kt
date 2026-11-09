package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.ReleaseOutside28WeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class ReleaseWithin28WeeksRuleSet(
  releaseOutside28Weeks: ReleaseOutside28WeeksRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(releaseOutside28Weeks)
  override val name: String = "PASS if person can start referral"
  override fun getRules(): List<Rule> = rules
}
