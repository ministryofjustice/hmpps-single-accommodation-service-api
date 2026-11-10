package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.HasNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.NextAccommodationData

@Component
class NoNextAccommodationRuleSet(
  hasNextAccommodation: HasNextAccommodationRule,
) : RuleSet<NextAccommodationData> {
  private val rules: List<Rule<NextAccommodationData>> = listOf(
    hasNextAccommodation,
  )

  override val name: String = "No Next Accommodation"

  override fun getRules(): List<Rule<NextAccommodationData>> = rules
}
