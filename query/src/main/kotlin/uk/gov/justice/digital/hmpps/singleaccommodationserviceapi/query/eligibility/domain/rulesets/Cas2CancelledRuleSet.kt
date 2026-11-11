package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotCancelledRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas2CancelledRuleSet(
  cas2NotCancelled: Cas2NotCancelledRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas2NotCancelled,
  )

  override val name: String = "PASS if the CAS2 assessment status is Cancelled"

  override fun getRules(): List<Rule> = rules
}
