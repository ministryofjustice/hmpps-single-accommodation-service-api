package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotOnWaitingListRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas2OnWaitingListRuleSet(
  cas2NotOnWaitingList: Cas2NotOnWaitingListRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas2NotOnWaitingList,
  )

  override val name: String = "PASS if the CAS2 assessment status is On Waiting List"

  override fun getRules(): List<Rule> = rules
}
