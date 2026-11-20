package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ApplicationSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1ApplicationNotSubmittedRuleSet(
  applicationSubmitted: Cas1ApplicationSubmittedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    applicationSubmitted,
  )

  override val name: String = "PASS if the CAS1 application has not been submitted"

  override fun getRules(): List<Rule> = rules
}
