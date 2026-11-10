package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.ReleaseOutside28WeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.CurrentAccommodationData

@Component
class ReleaseWithin28WeeksRuleSet(
  releaseOutside28Weeks: ReleaseOutside28WeeksRule,
) : RuleSet<CurrentAccommodationData> {
  private val rules: List<Rule<CurrentAccommodationData>> = listOf(releaseOutside28Weeks)
  override val name: String = "Release Within 28 Weeks"
  override fun getRules(): List<Rule<CurrentAccommodationData>> = rules
}
