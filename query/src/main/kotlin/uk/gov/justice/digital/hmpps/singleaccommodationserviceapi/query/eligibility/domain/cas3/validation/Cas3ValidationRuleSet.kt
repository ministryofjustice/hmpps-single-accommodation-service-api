package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.accommodation.CurrentAccommodationEndDateValidationRule

@Component
class Cas3ValidationRuleSet(
  currentAccommodationEndDate: CurrentAccommodationEndDateValidationRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(currentAccommodationEndDate)
  override fun getRules(): List<Rule> = rules
}
