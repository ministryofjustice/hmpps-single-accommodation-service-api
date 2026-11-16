package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotMarkedAsArrivedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas2MarkedAsArrivedRuleSet(
  notMarkedAsArrived: Cas2NotMarkedAsArrivedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    notMarkedAsArrived,
  )

  override val name: String = "PASS if the CAS2 arrival timestamp is present"

  override fun getRules(): List<Rule> = rules
}
