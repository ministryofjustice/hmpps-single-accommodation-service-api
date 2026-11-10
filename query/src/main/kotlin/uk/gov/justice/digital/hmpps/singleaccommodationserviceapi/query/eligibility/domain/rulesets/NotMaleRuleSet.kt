package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.MaleRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.SexData

@Component
class NotMaleRuleSet(
  isMale: MaleRule,
) : RuleSet<SexData> {
  private val rules: List<Rule<SexData>> = listOf(
    isMale,
  )

  override val name: String = "Not Male"

  override fun getRules(): List<Rule<SexData>> = rules
}
