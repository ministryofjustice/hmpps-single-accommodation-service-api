package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.rules.ReleaseOutside12WeeksRule

@Component
class ReleaseWithin12WeeksRuleSet(
  releaseOutside12Weeks: ReleaseOutside12WeeksRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(releaseOutside12Weeks))
  override val name: String = "Release Within 12 Weeks"
  override fun getRules(): List<PrioritisedRule> = rules
}
