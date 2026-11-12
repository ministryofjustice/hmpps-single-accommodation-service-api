package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NotBookingCancelledRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3BookingCancelledRuleSet(
  cas3NotBookingCancelled: Cas3NotBookingCancelledRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas3NotBookingCancelled)
  override val name: String = "PASS if the CAS3 booking status is Cancelled"
  override fun getRules(): List<Rule> = rules
}
