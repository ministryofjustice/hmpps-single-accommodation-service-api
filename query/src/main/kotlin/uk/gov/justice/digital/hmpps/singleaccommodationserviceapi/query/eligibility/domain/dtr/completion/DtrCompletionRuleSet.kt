package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class DtrCompletionRuleSet(
  applicationComplete: DtrApplicationCompleteRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(applicationComplete)
  override fun getRules(): List<Rule> = rules
}
