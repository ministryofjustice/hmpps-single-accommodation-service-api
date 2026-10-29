package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.ReleaseOutside15DaysRule

@Component
class ReleaseWithin15DaysRuleSet(
  releaseWithin15Days: ReleaseOutside15DaysRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(releaseWithin15Days))
  override val name: String = "Upcoming"
  override fun getRules(): List<PrioritisedRule> = rules
}
