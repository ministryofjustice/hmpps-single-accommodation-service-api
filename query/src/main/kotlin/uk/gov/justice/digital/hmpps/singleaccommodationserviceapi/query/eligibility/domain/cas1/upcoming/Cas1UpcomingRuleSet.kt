package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas1UpcomingRuleSet(
  releaseWithinTwentyEightWeeksRule: ReleaseWithinTwentyEightWeeksRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(releaseWithinTwentyEightWeeksRule))
  override val name: String = "Upcoming"
  override fun getRules(): List<PrioritisedRule> = rules
}
