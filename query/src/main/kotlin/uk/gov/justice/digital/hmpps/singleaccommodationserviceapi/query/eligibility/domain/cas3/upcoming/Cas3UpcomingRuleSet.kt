package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.RecentCurrentAccommodationEndDateRule

@Component
class Cas3UpcomingRuleSet(
  recentCurrentAccommodationEndDate: RecentCurrentAccommodationEndDateRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(recentCurrentAccommodationEndDate)
  override fun getRules(): List<Rule> = rules
}
