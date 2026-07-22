package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas1SuitabilityRuleSet(
  applicationPresent: Cas1ApplicationPresentRule,
  applicationSuitability: Cas1ApplicationSuitabilityRule,
  applicationRelevantExpired: Cas1ApplicationRelevantExpiredRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(applicationPresent, applicationRelevantExpired, applicationSuitability)
  override fun getRules(): List<Rule> = rules
}
