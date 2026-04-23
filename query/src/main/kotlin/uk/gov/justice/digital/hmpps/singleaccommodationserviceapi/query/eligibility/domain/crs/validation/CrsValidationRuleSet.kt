package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class CrsValidationRuleSet(
  private val rules: List<CrsValidationRule>,
) : RuleSet {
  override fun getRules(): List<Rule> = rules
}
