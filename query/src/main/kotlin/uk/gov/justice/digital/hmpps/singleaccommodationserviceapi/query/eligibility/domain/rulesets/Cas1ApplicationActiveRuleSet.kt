package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ApplicationNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1ApplicationActiveRuleSet(
  applicationNotActive: Cas1ApplicationNotActiveRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    applicationNotActive,
  )

  override val name: String = "PASS if the CAS1 application is active"

  override fun getRules(): List<Rule> = rules
}
