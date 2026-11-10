package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class MaleTierEligibleForCas1RuleSet(
  maleRiskEligibility: MaleRiskEligibilityRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    maleRiskEligibility,
  )

  override val name: String = "Male Tier Eligible For CAS1"

  override fun getRules(): List<Rule> = rules
}
