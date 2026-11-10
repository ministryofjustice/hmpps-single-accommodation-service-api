package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.ReleaseOutside8WeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.CurrentAccommodationData

@Component
class ReleaseWithin8WeeksRuleSet(
  releaseOutside8Weeks: ReleaseOutside8WeeksRule,
) : RuleSet<CurrentAccommodationData> {
  private val rules: List<Rule<CurrentAccommodationData>> = listOf(releaseOutside8Weeks)
  override val name: String = "Release Within 8 Weeks"
  override fun getRules(): List<Rule<CurrentAccommodationData>> = rules
}
