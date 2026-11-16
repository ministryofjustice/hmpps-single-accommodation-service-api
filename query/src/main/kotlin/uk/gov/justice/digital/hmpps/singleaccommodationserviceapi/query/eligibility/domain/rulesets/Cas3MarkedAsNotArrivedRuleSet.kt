package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NotMarkedAsNotArrivedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3MarkedAsNotArrivedRuleSet(
  cas3NotMarkedAsNotArrived: Cas3NotMarkedAsNotArrivedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas3NotMarkedAsNotArrived)
  override val name: String = "PASS if the CAS3 booking status is Not Arrived"
  override fun getRules(): List<Rule> = rules
}
