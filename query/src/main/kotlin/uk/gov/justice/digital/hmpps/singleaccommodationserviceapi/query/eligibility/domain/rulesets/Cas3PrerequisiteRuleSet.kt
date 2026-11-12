package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DtrExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NoLiveCrsRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3PrerequisiteRuleSet(
  dtrExpired: DtrExpiredRule,
  noLiveCrsRule: NoLiveCrsRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(dtrExpired, noLiveCrsRule)
  override val name: String = "PASS if the DTR was submitted in the last 26 weeks and there is a live CRS referral"
  override fun getRules(): List<Rule> = rules
}
