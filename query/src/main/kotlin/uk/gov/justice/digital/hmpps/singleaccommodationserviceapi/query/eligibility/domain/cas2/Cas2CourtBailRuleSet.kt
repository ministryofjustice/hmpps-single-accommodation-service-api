package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.rules.Cas2CourtBailRule

@Component
class Cas2CourtBailRuleSet(
  private val cas2Rules: List<Cas2CourtBailRule>,
) : RuleSet {
  override fun getRules(): List<Rule> = cas2Rules
}
