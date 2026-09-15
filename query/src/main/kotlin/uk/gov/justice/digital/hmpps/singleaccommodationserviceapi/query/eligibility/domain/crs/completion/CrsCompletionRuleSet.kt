package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsSubmittedRule

@Component
class CrsCompletionRuleSet(
  crsSubmitted: CrsSubmittedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(crsSubmitted)
  override fun getRules(): List<Rule> = rules
}
