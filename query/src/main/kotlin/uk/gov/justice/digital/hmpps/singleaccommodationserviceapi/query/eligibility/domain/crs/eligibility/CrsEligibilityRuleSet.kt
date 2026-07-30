package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.accommodation.NoNextAccommodationRule

@Component
class CrsEligibilityRuleSet(
  noNextAccommodation: NoNextAccommodationRule,
  isSettledRule: IsSettledRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(noNextAccommodation, isSettledRule)
  override fun getRules(): List<Rule> = rules
}
