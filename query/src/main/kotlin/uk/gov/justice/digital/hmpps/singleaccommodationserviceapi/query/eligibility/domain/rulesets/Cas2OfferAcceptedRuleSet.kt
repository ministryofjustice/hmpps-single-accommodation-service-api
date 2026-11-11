package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotOfferAcceptedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas2OfferAcceptedRuleSet(
  cas2NotOfferAccepted: Cas2NotOfferAcceptedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas2NotOfferAccepted,
  )

  override val name: String = "PASS if the CAS2 assessment status is Offer Accepted"

  override fun getRules(): List<Rule> = rules
}
