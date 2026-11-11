package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas2SubmittedRuleSet(
  cas2NotSubmitted: Cas2NotSubmittedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas2NotSubmitted)
  override val name: String = "PASS if the CAS2 application is submitted"
  override fun getRules(): List<Rule> = rules
}
