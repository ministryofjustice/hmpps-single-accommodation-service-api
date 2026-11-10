package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.ReleaseOutside15DaysRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.CurrentAccommodationData

@Component
class ReleaseWithin15DaysRuleSet(
  releaseOutside15Days: ReleaseOutside15DaysRule,
) : RuleSet<CurrentAccommodationData> {
  private val rules: List<Rule<CurrentAccommodationData>> = listOf(releaseOutside15Days)
  override val name: String = "Release Within 15 Days"
  override fun getRules(): List<Rule<CurrentAccommodationData>> = rules
}
