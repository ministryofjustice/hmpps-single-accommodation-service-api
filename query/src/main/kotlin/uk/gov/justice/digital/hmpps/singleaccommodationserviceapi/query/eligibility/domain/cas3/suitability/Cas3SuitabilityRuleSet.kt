package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas3SuitabilityRuleSet(
  applicationSuitability: Cas3ApplicationSuitabilityRule,
  applicationPresentSuitability: Cas3ApplicationPresentSuitabilityRule,
  bookingSuitability: Cas3BookingSuitabilityRule,
  assessmentSuitability: Cas3AssessmentSuitabilityRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(applicationSuitability),
    PrioritisedRule(applicationPresentSuitability),
    PrioritisedRule(bookingSuitability),
    PrioritisedRule(assessmentSuitability),
  )

  override fun getRules(): List<PrioritisedRule> = rules
}
