package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.rules.ReleaseOutside8WeeksRule

@Component
class ReleaseWithin8WeeksRuleSet(
  releaseOutside8Weeks: ReleaseOutside8WeeksRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(releaseOutside8Weeks)
  override val name: String = "Release Within 8 Weeks"
  override fun getRules(): List<Rule> = rules
}
