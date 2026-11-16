package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3ClosedBookingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3DepartedBookingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3ActiveBookingRuleSet(
  departedBooking: Cas3DepartedBookingRule,
  closedBooking: Cas3ClosedBookingRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(departedBooking, closedBooking)
  override val name: String = "PASS if the CAS3 booking status is not 'Departed' or 'Closed'"
  override fun getRules(): List<Rule> = rules
}
