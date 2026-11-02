package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.HasSexualOffenceRegistrationsRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.Under18Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.WrongTierRule

@Component
class PersonEligibleRuleSet(
  under18: Under18Rule,
  wrongTier: WrongTierRule,
  hasSexualOffenceRegistrations: HasSexualOffenceRegistrationsRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    under18,
    wrongTier,
    hasSexualOffenceRegistrations,
  )

  override val name: String = "Person Eligible"

  override fun getRules(): List<Rule> = rules
}
