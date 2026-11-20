package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1UnsubmittedRequestRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1SubmittedRequestRuleSet(
  unsubmittedRequest: Cas1UnsubmittedRequestRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    unsubmittedRequest,
  )

  override val name: String = "PASS if the CAS1 request for placement status is not 'Unsubmitted'"

  override fun getRules(): List<Rule> = rules
}
