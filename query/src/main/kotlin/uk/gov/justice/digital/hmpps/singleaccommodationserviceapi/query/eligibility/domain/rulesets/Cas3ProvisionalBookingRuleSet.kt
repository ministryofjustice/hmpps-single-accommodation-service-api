package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NotProvisionalBookingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3ProvisionalBookingRuleSet(
  notProvisionalBooking: Cas3NotProvisionalBookingRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(notProvisionalBooking)
  override val name: String = "PASS if the CAS3 booking status is 'Provisional'"
  override fun getRules(): List<Rule> = rules
}
