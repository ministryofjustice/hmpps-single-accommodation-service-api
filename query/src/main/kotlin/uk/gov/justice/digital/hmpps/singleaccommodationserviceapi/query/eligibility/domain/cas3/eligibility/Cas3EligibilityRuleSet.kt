package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRule

@Component
class Cas3EligibilityRuleSet(
  currentAccommodationType: CurrentAccommodationTypeRule,
  noNextAccommodation: NoNextAccommodationRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(currentAccommodationType, 0),
    PrioritisedRule(noNextAccommodation, 1),
  )

  override fun getRules(): List<PrioritisedRule> = rules
}
