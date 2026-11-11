package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotAwaitingArrivalRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas2AwaitingArrivalRuleSet(
  cas2NotAwaitingArrival: Cas2NotAwaitingArrivalRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas2NotAwaitingArrival,
  )

  override val name: String = "PASS if the CAS2 assessment status is Awaiting Arrival"

  override fun getRules(): List<Rule> = rules
}
