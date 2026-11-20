package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DtrNotAcceptedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class DtrAcceptedRuleSet(
  notAccepted: DtrNotAcceptedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    notAccepted,
  )

  override val name: String = "PASS if the DTR status is 'Accepted'"

  override fun getRules(): List<Rule> = rules
}
