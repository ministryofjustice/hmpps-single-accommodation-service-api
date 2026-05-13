package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.accommodation.NoNextAccommodationRule

@Component
class DtrEligibilityRuleSet(
  currentAddressTypeNotPrivate: CurrentAddressTypeNotPrivateRule,
  noNextAccommodation: NoNextAccommodationRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(currentAddressTypeNotPrivate, noNextAccommodation)
  override fun getRules(): List<Rule> = rules
}
