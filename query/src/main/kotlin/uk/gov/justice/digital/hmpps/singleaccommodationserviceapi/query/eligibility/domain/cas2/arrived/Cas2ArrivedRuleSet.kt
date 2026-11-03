package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.arrived

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.arrived.rules.Cas2NotArrivedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.arrived.rules.Cas2NotMarkedAsArrivedRule

@Component
class Cas2ArrivedRuleSet(
  cas2NotArrived: Cas2NotArrivedRule,
  cas2NotMarkedAsArrived: Cas2NotMarkedAsArrivedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas2NotArrived, cas2NotMarkedAsArrived)
  override val name: String = "CAS2 Arrived"
  override fun getRules(): List<Rule> = rules
}
