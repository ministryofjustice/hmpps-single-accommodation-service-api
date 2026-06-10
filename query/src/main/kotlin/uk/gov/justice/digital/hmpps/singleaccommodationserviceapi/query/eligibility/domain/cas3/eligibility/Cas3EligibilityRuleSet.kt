package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.accommodation.NoNextAccommodationRule

@Component
class Cas3EligibilityRuleSet(
  currentAccommodationType: CurrentAccommodationTypeRule,
  noNextAccommodation: NoNextAccommodationRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    currentAccommodationType,
    noNextAccommodation,
  )

  override fun getRules(): List<Rule> = rules
}
