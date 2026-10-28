package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class DtrCompletionRuleSet(
  applicationComplete: DtrApplicationCompleteRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(applicationComplete))
  override val name: String = "Completion"
  override fun getRules(): List<PrioritisedRule> = rules
}
