package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas2CompletionRuleSet(
  applicationCompletion: Cas2ApplicationCompletionRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(applicationCompletion)
  override fun getRules(): List<Rule> = rules
}
