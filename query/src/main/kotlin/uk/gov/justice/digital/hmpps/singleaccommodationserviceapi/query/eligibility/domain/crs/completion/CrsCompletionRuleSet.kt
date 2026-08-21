package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsSubmittedRule

@Component
class CrsCompletionRuleSet(
  crsSubmitted: CrsSubmittedRule,
  crsExpired: CrsExpiredRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(crsSubmitted, crsExpired)
  override fun getRules(): List<Rule> = rules
}
