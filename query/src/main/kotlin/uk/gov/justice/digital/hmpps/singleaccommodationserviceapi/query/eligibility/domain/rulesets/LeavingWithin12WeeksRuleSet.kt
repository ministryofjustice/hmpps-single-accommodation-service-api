package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.LeavingOutside12WeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class LeavingWithin12WeeksRuleSet(
  leavingOutside12Weeks: LeavingOutside12WeeksRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(leavingOutside12Weeks)
  override val name: String = "PASS if the leaving date is missing or within 12 weeks of today"
  override fun getRules(): List<Rule> = rules
}
