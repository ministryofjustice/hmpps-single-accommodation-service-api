package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.IsSettledRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.AccommodationTypeData

@Component
class NotSettledRuleSet(
  isSettled: IsSettledRule,
) : RuleSet<AccommodationTypeData> {
  private val rules: List<Rule<AccommodationTypeData>> = listOf(
    isSettled,
  )

  override val name: String = "Not Settled"

  override fun getRules(): List<Rule<AccommodationTypeData>> = rules
}
