package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.Cas3SuitabilityRule

@Component
class Cas3SuitabilityRuleSet(
  private val rules: List<Cas3SuitabilityRule>,
) : RuleSet {
  override fun getRules(): List<Rule> = rules
}
