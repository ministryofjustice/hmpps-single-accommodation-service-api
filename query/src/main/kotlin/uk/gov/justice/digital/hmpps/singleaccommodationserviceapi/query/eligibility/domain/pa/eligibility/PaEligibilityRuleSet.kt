package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class PaEligibilityRuleSet(
  cas1ApplicationNotSuitableRule: Cas1ApplicationNotSuitableRule,
  cas3ApplicationNotSuitableRule: Cas3ApplicationNotSuitableRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(cas1ApplicationNotSuitableRule, 0), PrioritisedRule(cas3ApplicationNotSuitableRule, 0))
  override val name: String = "Eligibility"
  override fun getRules(): List<PrioritisedRule> = rules
}
