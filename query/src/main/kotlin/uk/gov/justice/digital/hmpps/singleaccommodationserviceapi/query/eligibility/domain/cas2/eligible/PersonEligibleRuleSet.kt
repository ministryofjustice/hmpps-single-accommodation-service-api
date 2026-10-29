package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.HasSexualOffenceRegistrationsRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.Under18Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.WrongTierRule

@Component
class PersonEligibleRuleSet(
  under18: Under18Rule,
  singleLetterTier: WrongTierRule,
  sexualOffenceRegistrations: HasSexualOffenceRegistrationsRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(under18, 0),
    PrioritisedRule(singleLetterTier, 0),
    PrioritisedRule(sexualOffenceRegistrations, 0),
  )

  override val name: String = "Person Eligible"

  override fun getRules(): List<PrioritisedRule> = rules
}
