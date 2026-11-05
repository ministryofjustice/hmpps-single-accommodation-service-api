package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.ReleaseOutside12WeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class ReleaseWithin12WeeksRuleSet(
  releaseOutside12Weeks: ReleaseOutside12WeeksRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(releaseOutside12Weeks)
  override val name: String = "Release Within 12 Weeks"
  override fun getRules(): List<Rule> = rules
}
