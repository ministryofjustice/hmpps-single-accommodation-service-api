package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.rules.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.rules.NonMaleRiskEligibilityRule

@Component
class TierEligibleForCas1RuleSet(
  maleRiskEligibility: MaleRiskEligibilityRule,
  nonMaleRiskEligibility: NonMaleRiskEligibilityRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(maleRiskEligibility, 0),
    PrioritisedRule(nonMaleRiskEligibility, 0),
  )

  override val name: String = "Tier Eligible For CAS1"

  override fun getRules(): List<PrioritisedRule> = rules
}
