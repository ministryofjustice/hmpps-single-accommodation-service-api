package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas3EligibilityCurrentAccommodationRuleSet(
  currentAccommodationType: CurrentAccommodationTypeRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(currentAccommodationType, 0),
  )

  override val name: String = "Eligibility Current Accommodation"

  override fun getRules(): List<PrioritisedRule> = rules
}
