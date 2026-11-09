package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DtrExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DtrNotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DtrWithdrawnRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class DtrRuleSet(
  dtrNotPresent: DtrNotPresentRule,
  dtrWithdrawn: DtrWithdrawnRule,
  dtrExpired: DtrExpiredRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(dtrNotPresent, dtrWithdrawn, dtrExpired)
  override val name: String = "PASS if person has a valid DTR"
  override fun getRules(): List<Rule> = rules
}
