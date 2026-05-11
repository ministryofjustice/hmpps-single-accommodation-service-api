package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas3SuitabilityRuleSet(
  applicationSuitability: Cas3ApplicationSuitabilityRule,
  applicationPresentSuitability: Cas3ApplicationPresentSuitabilityRule,
  bookingSuitability: Cas3BookingSuitabilityRule,
  assessmentSuitability: Cas3AssessmentSuitabilityRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    applicationSuitability,
    applicationPresentSuitability,
    bookingSuitability,
    assessmentSuitability,
  )
  override fun getRules(): List<Rule> = rules
}
