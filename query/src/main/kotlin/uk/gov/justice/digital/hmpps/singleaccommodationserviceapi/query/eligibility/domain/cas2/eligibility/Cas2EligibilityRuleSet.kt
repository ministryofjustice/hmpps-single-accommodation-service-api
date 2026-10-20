package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRule

@Component
class Cas2EligibilityRuleSet(
  under18: Under18Rule,
  currentAccommodationTypeIsCas3: CurrentAccommodationTypeIsCas3Rule,
  currentAccommodationTypeIsCas2: CurrentAccommodationTypeIsCas2Rule,
  currentAccommodationTypeIsCommunity: CurrentAccommodationTypeIsCommunityRule,
  noNextAccommodation: NoNextAccommodationRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(under18, 0),
    PrioritisedRule(currentAccommodationTypeIsCas3, 2),
    PrioritisedRule(currentAccommodationTypeIsCas2, 2),
    PrioritisedRule(currentAccommodationTypeIsCommunity, 2),
    PrioritisedRule(noNextAccommodation, 3),
  )

  override fun getRules(): List<PrioritisedRule> = rules
}
