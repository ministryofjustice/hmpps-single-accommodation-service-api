package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.IsSettledRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class NotSettledRuleSet(
  isSettled: IsSettledRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    isSettled,
  )

  override val name: String = "PASS if person is not in settled accommodation"

  override fun getRules(): List<Rule> = rules
}
