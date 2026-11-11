package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotMarkedAsArrivedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas2MarkedAsArrivedRuleSet(
  cas2NotMarkedAsArrived: Cas2NotMarkedAsArrivedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas2NotMarkedAsArrived,
  )

  override val name: String = "PASS if CAS2 is marked as arrived"

  override fun getRules(): List<Rule> = rules
}
