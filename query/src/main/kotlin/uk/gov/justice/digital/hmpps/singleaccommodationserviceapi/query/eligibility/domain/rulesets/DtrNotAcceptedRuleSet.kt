package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DtrNotNotAcceptedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class DtrNotAcceptedRuleSet(
  notNotAccepted: DtrNotNotAcceptedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    notNotAccepted,
  )

  override val name: String = "PASS if the DTR status is 'Not Accepted'"

  override fun getRules(): List<Rule> = rules
}
