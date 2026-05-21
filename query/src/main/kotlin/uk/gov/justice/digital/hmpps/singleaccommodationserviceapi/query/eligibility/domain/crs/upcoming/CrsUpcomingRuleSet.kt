package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class CrsUpcomingRuleSet(
  crsUpcomingRule: CrsUpcomingRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(crsUpcomingRule)
  override fun getRules(): List<Rule> = rules
}
