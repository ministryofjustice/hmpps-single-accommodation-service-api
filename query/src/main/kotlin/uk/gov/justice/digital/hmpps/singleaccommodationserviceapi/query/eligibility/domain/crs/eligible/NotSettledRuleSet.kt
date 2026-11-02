package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligible

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligible.rules.IsSettledRule

@Component
class NotSettledRuleSet(
  isSettled: IsSettledRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    isSettled,
  )

  override val name: String = "Not Settled"

  override fun getRules(): List<Rule> = rules
}
