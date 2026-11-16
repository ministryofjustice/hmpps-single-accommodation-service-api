package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotMoreInformationRequestedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas2MoreInformationRequestedRuleSet(
  notMoreInformationRequested: Cas2NotMoreInformationRequestedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    notMoreInformationRequested,
  )

  override val name: String = "PASS if the CAS2 assessment status is 'More Information Requested'"

  override fun getRules(): List<Rule> = rules
}
