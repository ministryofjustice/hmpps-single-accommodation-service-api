package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3PresentRuleSet(
  cas3NotPresent: Cas3NotPresentRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas3NotPresent,
  )
  override val name: String = "PASS if a CAS3 application exists"
  override fun getRules(): List<Rule> = rules
}
