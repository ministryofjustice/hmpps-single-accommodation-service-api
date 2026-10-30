package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.rules.ReleaseOutside28WeeksRule

@Component
class ReleaseWithin28WeeksRuleSet(
  releaseOutside28Weeks: ReleaseOutside28WeeksRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(releaseOutside28Weeks))
  override val name: String = "Release Within 28 Weeks"
  override fun getRules(): List<PrioritisedRule> = rules
}
