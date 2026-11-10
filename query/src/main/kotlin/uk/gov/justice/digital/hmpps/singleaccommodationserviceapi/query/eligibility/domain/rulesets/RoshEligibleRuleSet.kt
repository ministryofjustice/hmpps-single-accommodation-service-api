package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NoRoshLevelRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class RoshEligibleRuleSet(
  noRoshLevel: NoRoshLevelRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    noRoshLevel,
  )
  override val name: String = "ROSH Eligible"

  override fun getRules(): List<Rule> = rules
}
