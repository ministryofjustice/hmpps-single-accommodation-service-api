package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotPlaceOfferedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas2PlaceOfferedRuleSet(
  notPlaceOffered: Cas2NotPlaceOfferedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    notPlaceOffered,
  )

  override val name: String = "PASS if the CAS2 assessment status is 'Place Offered'"

  override fun getRules(): List<Rule> = rules
}
