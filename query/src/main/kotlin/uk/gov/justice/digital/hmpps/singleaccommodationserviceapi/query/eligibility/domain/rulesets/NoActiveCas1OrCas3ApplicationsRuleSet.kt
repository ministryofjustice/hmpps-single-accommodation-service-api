package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ApplicationActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3ApplicationActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class NoActiveCas1OrCas3ApplicationsRuleSet(
  cas1ApplicationActive: Cas1ApplicationActiveRule,
  cas3ApplicationActive: Cas3ApplicationActiveRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas1ApplicationActive, cas3ApplicationActive)
  override val name: String = "PASS if the CAS1 application is not active and the CAS3 application is not active"
  override fun getRules(): List<Rule> = rules
}
