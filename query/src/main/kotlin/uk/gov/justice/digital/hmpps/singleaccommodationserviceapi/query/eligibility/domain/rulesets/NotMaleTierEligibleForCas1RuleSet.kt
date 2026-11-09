package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class NotMaleTierEligibleForCas1RuleSet(
  nonMaleRiskEligibility: NonMaleRiskEligibilityRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    nonMaleRiskEligibility,
  )

  override val name: String = "PASS if person is Tier A, B, C or D"

  override fun getRules(): List<Rule> = rules
}
