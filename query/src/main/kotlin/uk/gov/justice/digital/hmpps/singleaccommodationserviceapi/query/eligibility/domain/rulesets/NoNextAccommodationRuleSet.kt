package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.HasNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

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
