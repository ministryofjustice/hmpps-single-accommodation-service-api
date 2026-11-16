package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.MaleRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class NotMaleRuleSet(
  male: MaleRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    male,
  )

  override val name: String = "PASS if the person is not Male"

  override fun getRules(): List<Rule> = rules
}
