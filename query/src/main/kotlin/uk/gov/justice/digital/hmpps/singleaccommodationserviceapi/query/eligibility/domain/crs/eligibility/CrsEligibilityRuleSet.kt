package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRule

@Component
class CrsEligibilityRuleSet(
  noNextAccommodation: NoNextAccommodationRule,
  isSettledRule: IsSettledRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(noNextAccommodation, 1), PrioritisedRule(isSettledRule, 0))
  override fun getRules(): List<PrioritisedRule> = rules
}
