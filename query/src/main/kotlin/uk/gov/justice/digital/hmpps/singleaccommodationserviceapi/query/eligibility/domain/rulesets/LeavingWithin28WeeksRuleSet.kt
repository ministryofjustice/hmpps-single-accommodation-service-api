package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.LeavingOutside28WeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class LeavingWithin28WeeksRuleSet(
  leavingOutside28Weeks: LeavingOutside28WeeksRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(leavingOutside28Weeks)
  override val name: String = "PASS if the leaving date is missing or within 28 weeks of today"
  override fun getRules(): List<Rule> = rules
}
