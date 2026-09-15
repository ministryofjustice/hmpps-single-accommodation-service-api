package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas2SuitabilityRuleSet(
  applicationSubmitted: Cas2ApplicationSubmittedRule,
  suitableStatus: Cas2SuitableStatusRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(applicationSubmitted, suitableStatus)
  override fun getRules(): List<Rule> = rules
}
