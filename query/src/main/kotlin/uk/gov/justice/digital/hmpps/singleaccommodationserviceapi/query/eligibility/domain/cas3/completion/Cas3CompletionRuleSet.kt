package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas3CompletionRuleSet(
  applicationArrived: Cas3ApplicationArrivedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(applicationArrived)
  override fun getRules(): List<Rule> = rules
}
