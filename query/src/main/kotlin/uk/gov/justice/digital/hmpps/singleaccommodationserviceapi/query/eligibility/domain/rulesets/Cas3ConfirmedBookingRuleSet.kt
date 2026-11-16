package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NotConfirmedBookingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3ConfirmedBookingRuleSet(
  notConfirmedBooking: Cas3NotConfirmedBookingRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(notConfirmedBooking)
  override val name: String = "PASS if the CAS3 booking status is 'Confirmed'"
  override fun getRules(): List<Rule> = rules
}
