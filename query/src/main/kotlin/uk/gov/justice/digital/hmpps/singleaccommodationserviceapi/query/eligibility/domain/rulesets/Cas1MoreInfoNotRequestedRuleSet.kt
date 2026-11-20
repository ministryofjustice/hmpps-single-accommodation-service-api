package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1MoreInfoRequestedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1MoreInfoNotRequestedRuleSet(
  moreInfoRequested: Cas1MoreInfoRequestedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    moreInfoRequested,
  )

  override val name: String = "PASS if the CAS1 application status is not 'Requested Further Information'"

  override fun getRules(): List<Rule> = rules
}
