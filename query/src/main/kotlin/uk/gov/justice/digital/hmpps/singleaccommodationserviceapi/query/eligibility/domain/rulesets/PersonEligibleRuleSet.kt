package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.HasSexualOffenceRegistrationsRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Under18Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.WrongTierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

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
