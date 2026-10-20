package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class CrsCompletionRuleSet(
  crsSubmitted: CrsSubmittedRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(crsSubmitted))
  override fun getRules(): List<PrioritisedRule> = rules
}
