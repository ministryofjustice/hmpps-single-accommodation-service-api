package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CurrentAccommodationEndDateValidationRule

@Component
class Cas1ValidationRuleSet(
  currentAccommodationEndDate: CurrentAccommodationEndDateValidationRule,
  sexValidation: Cas1SexValidationRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(currentAccommodationEndDate, sexValidation)
  override fun getRules(): List<Rule> = rules
}
