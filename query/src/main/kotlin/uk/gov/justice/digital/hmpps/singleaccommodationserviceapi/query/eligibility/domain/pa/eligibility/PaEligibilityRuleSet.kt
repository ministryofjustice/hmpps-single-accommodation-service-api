package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class PaEligibilityRuleSet(
  cas1ApplicationNotSuitableRule: Cas1ApplicationNotSuitableRule,
  cas3ApplicationNotSuitableRule: Cas3ApplicationNotSuitableRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas1ApplicationNotSuitableRule, cas3ApplicationNotSuitableRule)
  override fun getRules(): List<Rule> = rules
}
