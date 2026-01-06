package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.Cas3EligibilityRule

@Component
class Cas3EligibilityRuleSet(
  private val rules: List<Cas3EligibilityRule>,
) : RuleSet {
  override fun getRules(): List<Rule> = rules
}
