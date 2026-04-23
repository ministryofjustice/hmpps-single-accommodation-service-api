package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class CrsSuitabilityRuleSet(
  private val rules: List<CrsSuitabilityRule>,
) : RuleSet {
  override fun getRules(): List<Rule> = rules
}
