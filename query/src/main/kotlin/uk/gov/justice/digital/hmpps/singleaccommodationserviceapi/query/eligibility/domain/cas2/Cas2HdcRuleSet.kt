package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.rules.Cas2HdcRule

@Component
class Cas2HdcRuleSet(
  private val cas2Rules: List<Cas2HdcRule>,
) : RuleSet {
  override fun getRules(): List<Rule> = cas2Rules
}
