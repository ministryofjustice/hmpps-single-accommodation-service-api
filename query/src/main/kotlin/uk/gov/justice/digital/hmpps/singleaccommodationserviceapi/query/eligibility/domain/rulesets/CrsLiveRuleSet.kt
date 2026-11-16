package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CrsNotLiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class CrsLiveRuleSet(
  notLive: CrsNotLiveRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(notLive)
  override val name: String = "PASS if there is a live CRS referral"
  override fun getRules(): List<Rule> = rules
}
