package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas1SuitabilityRuleSet(
  applicationSuitability: Cas1ApplicationSuitabilityRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(applicationSuitability)
  override fun getRules(): List<Rule> = rules
}
