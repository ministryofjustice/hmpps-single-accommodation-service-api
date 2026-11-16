package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.LeavingOutside15DaysRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class LeavingWithin15DaysRuleSet(
  leavingOutside15Days: LeavingOutside15DaysRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(leavingOutside15Days)
  override val name: String = "PASS if the leaving date is missing or within 15 days of today"
  override fun getRules(): List<Rule> = rules
}
