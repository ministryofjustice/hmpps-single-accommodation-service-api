package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3AssessmentClosedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas3AssessmentNotClosedRuleSet(
  cas3AssessmentClosed: Cas3AssessmentClosedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas3AssessmentClosed)
  override val name: String = "PASS if the CAS3 assessment status is not Closed"
  override fun getRules(): List<Rule> = rules
}
