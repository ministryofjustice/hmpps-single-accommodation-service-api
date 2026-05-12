package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas1EligibilityRuleSet(
  sTier: STierEligibilityRule,
  maleRisk: MaleRiskEligibilityRule,
  nonMaleRisk: NonMaleRiskEligibilityRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(sTier, maleRisk, nonMaleRisk)
  override fun getRules(): List<Rule> = rules
}
