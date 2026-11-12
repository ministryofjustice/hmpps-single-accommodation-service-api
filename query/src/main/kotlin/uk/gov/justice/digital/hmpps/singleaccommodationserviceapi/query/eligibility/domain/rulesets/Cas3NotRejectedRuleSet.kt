package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3ApplicationRejectedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3AssessmentRejectedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3NotRejectedRuleSet(
  cas3AssessmentRejected: Cas3AssessmentRejectedRule,
  cas3ApplicationRejected: Cas3ApplicationRejectedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas3AssessmentRejected, cas3ApplicationRejected)
  override val name: String = "PASS if the CAS3 referral has not been rejected"
  override fun getRules(): List<Rule> = rules
}
