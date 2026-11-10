package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.TierData

@Component
class NotMaleTierEligibleForCas1RuleSet(
  nonMaleRiskEligibility: NonMaleRiskEligibilityRule,
) : RuleSet<TierData> {
  private val rules: List<Rule<TierData>> = listOf(
    nonMaleRiskEligibility,
  )

  override val name: String = "Not Male Tier Eligible For CAS1"

  override fun getRules(): List<Rule<TierData>> = rules
}
