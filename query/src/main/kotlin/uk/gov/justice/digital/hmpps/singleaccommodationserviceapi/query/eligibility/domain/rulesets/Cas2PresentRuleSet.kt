package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas2PresentRuleSet(
  cas2NotPresent: Cas2NotPresentRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas2NotPresent)
  override val name: String = "PASS if a CAS2 application exists"
  override fun getRules(): List<Rule> = rules
}
