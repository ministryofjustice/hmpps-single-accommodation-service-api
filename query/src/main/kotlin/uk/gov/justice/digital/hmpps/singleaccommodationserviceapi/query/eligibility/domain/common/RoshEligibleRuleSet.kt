package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.NoRoshLevelRule

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
