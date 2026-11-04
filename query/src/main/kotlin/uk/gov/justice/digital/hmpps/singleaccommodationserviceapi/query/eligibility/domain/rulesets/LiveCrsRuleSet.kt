package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NoLiveCrsRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class LiveCrsRuleSet(
  noLiveCrs: NoLiveCrsRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(noLiveCrs)
  override val name: String = "Live CRS"
  override fun getRules(): List<Rule> = rules
}
