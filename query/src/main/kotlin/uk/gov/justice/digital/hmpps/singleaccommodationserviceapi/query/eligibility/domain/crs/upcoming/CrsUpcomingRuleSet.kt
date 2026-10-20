package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class CrsUpcomingRuleSet(
  crsUpcomingRule: CrsUpcomingRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(crsUpcomingRule))
  override fun getRules(): List<PrioritisedRule> = rules
}
