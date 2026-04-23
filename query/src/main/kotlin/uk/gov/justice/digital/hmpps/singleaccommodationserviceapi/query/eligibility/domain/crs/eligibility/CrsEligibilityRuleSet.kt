package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class CrsEligibilityRuleSet(
  private val rules: List<CrsEligibilityRule>,
) : RuleSet {
  override fun getRules(): List<Rule> = rules
}
