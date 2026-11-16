package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NotArrivedBookingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3ArrivedBookingRuleSet(
  notArrivedBooking: Cas3NotArrivedBookingRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(notArrivedBooking)
  override val name: String = "PASS if the CAS3 booking status is 'Arrived'"
  override fun getRules(): List<Rule> = rules
}
