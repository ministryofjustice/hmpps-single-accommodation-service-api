package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NotSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3SubmittedRuleSet(
  notSubmitted: Cas3NotSubmittedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    notSubmitted,
  )
  override val name: String = "PASS if the CAS3 application is submitted"
  override fun getRules(): List<Rule> = rules
}
