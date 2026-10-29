package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.referral

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.referral.rules.NoNextAccommodationRule

@Component
class HasNextAccommodationRuleSet(
  noNextAccommodationRule: NoNextAccommodationRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(noNextAccommodationRule))
  override val name: String = "Has Next Accommodation"
  override fun getRules(): List<PrioritisedRule> = rules
}
