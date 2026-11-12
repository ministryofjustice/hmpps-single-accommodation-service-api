package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NotBookingConfirmedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3BookingConfirmedRuleSet(
  cas3NotBookingConfirmed: Cas3NotBookingConfirmedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas3NotBookingConfirmed)
  override val name: String = "PASS if the CAS3 booking status is Confirmed"
  override fun getRules(): List<Rule> = rules
}
