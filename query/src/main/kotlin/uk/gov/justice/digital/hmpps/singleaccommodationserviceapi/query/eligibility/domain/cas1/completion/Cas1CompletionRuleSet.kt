package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas1CompletionRuleSet(
  applicationCompletion: Cas1ApplicationCompletionRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(applicationCompletion)
  override fun getRules(): List<Rule> = rules
}
