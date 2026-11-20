package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.TierNotAToCRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class TierAToCRuleSet(
  tierNotAToC: TierNotAToCRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    tierNotAToC,
  )

  override val name: String = "PASS if the person's tier is A, B or C"

  override fun getRules(): List<Rule> = rules
}
