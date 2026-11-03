package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.ReleaseOutside15DaysRule

@Component
class ReleaseWithin15DaysRuleSet(
  releaseOutside15Days: ReleaseOutside15DaysRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(releaseOutside15Days)
  override val name: String = "Release Within 15 Days"
  override fun getRules(): List<Rule> = rules
}
