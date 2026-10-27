package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas1EligibilityHighPriorityRuleSet(
  maleRisk: MaleRiskEligibilityRule,
  nonMaleRisk: NonMaleRiskEligibilityRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(maleRisk, 0),
    PrioritisedRule(nonMaleRisk, 0),
  )

  override fun getRules(): List<PrioritisedRule> = rules
}

@Component
class Cas1EligibilityLowPriorityRuleSet(
  currentAccommodation: CurrentAccommodationIsPrisonOrCas1Rule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(currentAccommodation, 1),
  )

  override fun getRules(): List<PrioritisedRule> = rules
}
