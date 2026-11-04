package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class TierEligibleForCas1RuleSet(
  maleRiskEligibility: MaleRiskEligibilityRule,
  nonMaleRiskEligibility: NonMaleRiskEligibilityRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    maleRiskEligibility,
    nonMaleRiskEligibility,
  )

  override val name: String = "Tier Eligible For CAS1"

  override fun getRules(): List<Rule> = rules
}
