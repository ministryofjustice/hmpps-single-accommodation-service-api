package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.accommodation.NoNextAccommodationRule

@Component
class CrsEligibilityRuleSet(
  noNextAccommodation: NoNextAccommodationRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(noNextAccommodation)
  override fun getRules(): List<Rule> = rules
}
