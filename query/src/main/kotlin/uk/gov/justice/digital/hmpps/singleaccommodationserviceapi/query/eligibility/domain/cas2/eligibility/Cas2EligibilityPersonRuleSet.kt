package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas2EligibilityPersonRuleSet(
  under18: Under18Rule,
  singleLetterTier: SingleLetterTierRule,
  sexualOffenceRegistrations: SexualOffenceRegistrationsRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(under18, 0),
    PrioritisedRule(singleLetterTier, 0),
    PrioritisedRule(sexualOffenceRegistrations, 0),
  )

  override fun getRules(): List<PrioritisedRule> = rules
}
