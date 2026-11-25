package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.PpcControlMeasuresSuitableRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.PpcPracticalSupportSufficientRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.PpcSupervisionRequiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class PpcPublicProtectionNotRequiredRuleSet(
  private val controlMeasuresSuitable: PpcControlMeasuresSuitableRule,
  private val supervisionRequired: PpcSupervisionRequiredRule,
  private val practicalSupportSufficient: PpcPracticalSupportSufficientRule,
) : RuleSet {
  override val name = "PASS if Public Protection not required"

  override fun getRules(): List<Rule> = listOf(controlMeasuresSuitable, supervisionRequired, practicalSupportSufficient)
}
