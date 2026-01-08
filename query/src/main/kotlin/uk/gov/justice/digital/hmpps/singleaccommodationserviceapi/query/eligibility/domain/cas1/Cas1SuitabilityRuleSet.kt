package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.Cas1SuitabilityRule

@Component
class Cas1SuitabilityRuleSet(
  private val rules: List<Cas1SuitabilityRule>,
) : RuleSet {
  override fun getRules(): List<Rule> = rules
}
