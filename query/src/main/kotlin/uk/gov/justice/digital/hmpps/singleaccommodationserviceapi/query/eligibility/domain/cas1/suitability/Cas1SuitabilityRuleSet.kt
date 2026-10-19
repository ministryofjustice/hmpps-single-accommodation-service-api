package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas1SuitabilityRuleSet(
  applicationPresent: Cas1ApplicationPresentRule,
  applicationSuitability: Cas1ApplicationSuitabilityRule,
  applicationRelevantExpired: Cas1ApplicationRelevantExpiredRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(applicationPresent),
    PrioritisedRule(applicationRelevantExpired),
    PrioritisedRule(applicationSuitability),
  )

  override fun getRules(): List<PrioritisedRule> = rules
}
