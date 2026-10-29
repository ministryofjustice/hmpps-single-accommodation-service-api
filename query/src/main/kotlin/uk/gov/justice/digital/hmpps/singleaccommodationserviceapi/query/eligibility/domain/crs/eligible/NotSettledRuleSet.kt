package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligible

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligible.rules.IsSettledRule

@Component
class NotSettledRuleSet(
  isSettled: IsSettledRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(isSettled, 0),
  )

  override val name: String = "Eligibility Settled"

  override fun getRules(): List<PrioritisedRule> = rules
}
