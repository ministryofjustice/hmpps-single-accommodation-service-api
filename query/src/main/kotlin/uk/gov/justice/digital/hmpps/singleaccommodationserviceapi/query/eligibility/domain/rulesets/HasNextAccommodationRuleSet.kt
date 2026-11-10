package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NoNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.NextAccommodationData

@Component
class HasNextAccommodationRuleSet(
  noNextAccommodation: NoNextAccommodationRule,
) : RuleSet<NextAccommodationData> {
  private val rules: List<Rule<NextAccommodationData>> = listOf(noNextAccommodation)
  override val name: String = "Has Next Accommodation"
  override fun getRules(): List<Rule<NextAccommodationData>> = rules
}
