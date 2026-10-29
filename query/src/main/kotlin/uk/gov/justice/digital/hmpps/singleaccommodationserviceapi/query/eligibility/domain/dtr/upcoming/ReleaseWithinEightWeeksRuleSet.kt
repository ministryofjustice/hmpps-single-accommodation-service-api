package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.rules.ReleaseOutsideEightWeeksRule

@Component
class ReleaseWithinEightWeeksRuleSet(
  releaseWithinEightWeeks: ReleaseOutsideEightWeeksRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(releaseWithinEightWeeks))
  override val name: String = "Upcoming"
  override fun getRules(): List<PrioritisedRule> = rules
}
