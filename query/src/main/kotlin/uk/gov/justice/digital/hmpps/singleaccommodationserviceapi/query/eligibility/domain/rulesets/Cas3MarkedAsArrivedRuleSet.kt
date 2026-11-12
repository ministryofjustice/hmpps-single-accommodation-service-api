package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NotMarkedAsArrivedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3MarkedAsArrivedRuleSet(
  cas3NotMarkedAsArrived: Cas3NotMarkedAsArrivedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas3NotMarkedAsArrived)
  override val name: String = "PASS if the CAS3 booking status is Arrived"
  override fun getRules(): List<Rule> = rules
}
