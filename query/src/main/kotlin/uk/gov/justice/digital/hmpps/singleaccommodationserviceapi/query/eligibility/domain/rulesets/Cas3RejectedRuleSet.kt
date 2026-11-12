package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NotRejectedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3RejectedRuleSet(
  cas3NotRejected: Cas3NotRejectedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas3NotRejected,
  )
  override val name: String = "PASS if there is no booking and the assessment or application is rejected"
  override fun getRules(): List<Rule> = rules
}
