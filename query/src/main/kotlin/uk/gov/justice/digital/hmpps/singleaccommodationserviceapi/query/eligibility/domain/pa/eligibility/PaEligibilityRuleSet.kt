package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class PaEligibilityRuleSet(
  private val rules: List<PaEligibilityRule>,
) : RuleSet {
  override fun getRules(): List<Rule> = rules
}
