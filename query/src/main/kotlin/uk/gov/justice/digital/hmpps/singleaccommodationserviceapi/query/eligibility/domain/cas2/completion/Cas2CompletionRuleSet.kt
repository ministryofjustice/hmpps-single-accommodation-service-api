package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas2CompletionRuleSet(
  applicationAwaitingArrival: Cas2ApplicationAwaitingArrivalRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(applicationAwaitingArrival)
  override fun getRules(): List<Rule> = rules
}
