package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class DtrUpcomingRuleSet(
  releaseWithinEightWeeks: ReleaseWithinEightWeeksRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(releaseWithinEightWeeks)
  override fun getRules(): List<Rule> = rules
}
