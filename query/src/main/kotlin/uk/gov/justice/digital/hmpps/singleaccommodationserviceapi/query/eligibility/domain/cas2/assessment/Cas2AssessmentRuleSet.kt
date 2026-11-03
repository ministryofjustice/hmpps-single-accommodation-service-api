package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.assessment

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.assessment.rules.Cas2NotActiveRule

@Component
class Cas2AssessmentRuleSet(
  cas2NotActive: Cas2NotActiveRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas2NotActive)
  override val name: String = "CAS2 Assessment"
  override fun getRules(): List<Rule> = rules
}
