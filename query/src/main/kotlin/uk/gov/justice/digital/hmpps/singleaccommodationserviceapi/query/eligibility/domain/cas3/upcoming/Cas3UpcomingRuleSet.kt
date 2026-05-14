package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas3UpcomingRuleSet(
  releaseWithinFourWeeksRule: ReleaseWithinFourWeeksRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(releaseWithinFourWeeksRule)
  override fun getRules(): List<Rule> = rules
}
