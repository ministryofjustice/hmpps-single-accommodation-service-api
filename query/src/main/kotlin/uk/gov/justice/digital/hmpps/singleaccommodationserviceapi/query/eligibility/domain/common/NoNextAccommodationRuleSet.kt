package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.HasNextAccommodationRule

@Component
class NoNextAccommodationRuleSet(
  hasNextAccommodation: HasNextAccommodationRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(hasNextAccommodation, 3),
  )

  override val name: String = "Eligibility Next Accommodation"

  override fun getRules(): List<PrioritisedRule> = rules
}
