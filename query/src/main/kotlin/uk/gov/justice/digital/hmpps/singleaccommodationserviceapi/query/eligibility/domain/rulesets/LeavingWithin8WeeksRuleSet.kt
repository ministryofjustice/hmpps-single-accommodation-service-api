package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.LeavingOutside8WeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class LeavingWithin8WeeksRuleSet(
  leavingOutside8Weeks: LeavingOutside8WeeksRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(leavingOutside8Weeks)
  override val name: String = "PASS if the leaving date is missing or within 8 weeks of today"
  override fun getRules(): List<Rule> = rules
}
