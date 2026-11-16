package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.TierNotAToDRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class TierAToDRuleSet(
  tierNotAToD: TierNotAToDRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    tierNotAToD,
  )

  override val name: String = "PASS if the person's tier is A, B, C or D"

  override fun getRules(): List<Rule> = rules
}
