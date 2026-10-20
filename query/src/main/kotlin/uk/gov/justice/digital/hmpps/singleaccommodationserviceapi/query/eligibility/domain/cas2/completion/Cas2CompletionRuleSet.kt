package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas2CompletionRuleSet(
  applicationAwaitingArrival: Cas2ApplicationAwaitingArrivalRule,
  arrivedRule: Cas2ApplicationArrivedRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(applicationAwaitingArrival),
    PrioritisedRule(arrivedRule),
  )
  override fun getRules(): List<PrioritisedRule> = rules
}
