package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.rules.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.rules.NonMaleRiskEligibilityRule

@Component
class TierEligibleForCas1RuleSet(
  maleRisk: MaleRiskEligibilityRule,
  nonMaleRisk: NonMaleRiskEligibilityRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(maleRisk, 0),
    PrioritisedRule(nonMaleRisk, 0),
  )

  override val name: String = "Tier Eligible For CAS1"

  override fun getRules(): List<PrioritisedRule> = rules
}
