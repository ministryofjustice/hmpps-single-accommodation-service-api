package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NoBookingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3HasBookingRuleSet(
  noBooking: Cas3NoBookingRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(noBooking)
  override val name: String = "PASS if there is a CAS3 booking status"
  override fun getRules(): List<Rule> = rules
}
