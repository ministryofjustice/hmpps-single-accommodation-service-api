package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.TierData

@Component
class MaleTierEligibleForCas1RuleSet(
  maleRiskEligibility: MaleRiskEligibilityRule,
) : RuleSet<TierData> {
  private val rules: List<Rule<TierData>> = listOf(
    maleRiskEligibility,
  )

  override val name: String = "Male Tier Eligible For CAS1"

  override fun getRules(): List<Rule<TierData>> = rules
}
