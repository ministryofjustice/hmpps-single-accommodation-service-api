package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NoNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class HasNextAccommodationRuleSet(
  noNextAccommodation: NoNextAccommodationRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(noNextAccommodation)
  override val name: String = "PASS if person has a next accommodation"
  override fun getRules(): List<Rule> = rules
}
