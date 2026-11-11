package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas2ActiveRuleSet(
  cas2NotActive: Cas2NotActiveRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas2NotActive)
  override val name: String = "PASS if the CAS2 assessment status is active"
  override fun getRules(): List<Rule> = rules
}
