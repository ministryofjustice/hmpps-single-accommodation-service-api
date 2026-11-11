package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1RejectedRequestRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1NotRejectedRequestRuleSet(
  rejectedRequest: Cas1RejectedRequestRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    rejectedRequest,
  )

  override val name: String = "PASS if placement is not rejected"

  override fun getRules(): List<Rule> = rules
}
