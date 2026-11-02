package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.HasNextAccommodationRule

@Component
class NoNextAccommodationRuleSet(
  hasNextAccommodation: HasNextAccommodationRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    hasNextAccommodation,
  )

  override val name: String = "No Next Accommodation"

  override fun getRules(): List<Rule> = rules
}
