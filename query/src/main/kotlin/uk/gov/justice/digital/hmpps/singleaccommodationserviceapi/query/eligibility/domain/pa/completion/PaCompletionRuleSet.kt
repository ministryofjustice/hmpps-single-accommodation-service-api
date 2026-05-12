package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class PaCompletionRuleSet(
  hasNextAccommodationRule: HasNextAccommodationRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(hasNextAccommodationRule)
  override fun getRules(): List<Rule> = rules
}
