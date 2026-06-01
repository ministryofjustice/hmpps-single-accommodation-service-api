package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas1ValidationRuleSet(
  sexValidation: Cas1SexValidationRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(sexValidation)
  override fun getRules(): List<Rule> = rules
}
