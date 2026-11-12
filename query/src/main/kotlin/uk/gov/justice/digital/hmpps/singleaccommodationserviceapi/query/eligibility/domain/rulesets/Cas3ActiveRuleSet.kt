package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3AssessmentNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3BookingNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3ActiveRuleSet(
  cas3BookingNotActive: Cas3BookingNotActiveRule,
  cas3AssessmentNotActive: Cas3AssessmentNotActiveRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas3BookingNotActive,
    cas3AssessmentNotActive,
  )
  override val name: String = "PASS if the booking is not departed or closed, and the assessment is not closed (when there is no booking)"
  override fun getRules(): List<Rule> = rules
}
