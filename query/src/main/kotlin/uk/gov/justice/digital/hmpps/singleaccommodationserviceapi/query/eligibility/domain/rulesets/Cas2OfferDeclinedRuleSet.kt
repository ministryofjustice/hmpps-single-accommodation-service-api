package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotOfferDeclinedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas2OfferDeclinedRuleSet(
  notOfferDeclined: Cas2NotOfferDeclinedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    notOfferDeclined,
  )

  override val name: String = "PASS if the CAS2 assessment status is 'Offer Declined'"

  override fun getRules(): List<Rule> = rules
}
