package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ApplicationNotRejectedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1ApplicationRejectedRuleSet(
  applicationNotRejected: Cas1ApplicationNotRejectedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    applicationNotRejected,
  )

  override val name: String = "PASS if the CAS1 application status is 'Rejected'"

  override fun getRules(): List<Rule> = rules
}
