package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotMoreInformationNeededRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas2MoreInformationNeededRuleSet(
  cas2NotMoreInformationNeeded: Cas2NotMoreInformationNeededRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas2NotMoreInformationNeeded,
  )

  override val name: String = "PASS if the CAS2 assessment status is More Information Requested"

  override fun getRules(): List<Rule> = rules
}
