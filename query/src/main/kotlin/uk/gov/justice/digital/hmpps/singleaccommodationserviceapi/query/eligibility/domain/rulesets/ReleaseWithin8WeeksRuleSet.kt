package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.ReleaseOutside8WeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class ReleaseWithin8WeeksRuleSet(
  releaseOutside8Weeks: ReleaseOutside8WeeksRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(releaseOutside8Weeks)
  override val name: String = "Release Within 8 Weeks"
  override fun getRules(): List<Rule> = rules
}
