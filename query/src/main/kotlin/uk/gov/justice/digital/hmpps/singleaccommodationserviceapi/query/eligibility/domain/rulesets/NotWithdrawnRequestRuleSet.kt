package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.WithdrawnRequestRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class NotWithdrawnRequestRuleSet(
  withdrawnRequest: WithdrawnRequestRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    withdrawnRequest,
  )

  override val name: String = "PASS if placement is not withdrawn"

  override fun getRules(): List<Rule> = rules
}
