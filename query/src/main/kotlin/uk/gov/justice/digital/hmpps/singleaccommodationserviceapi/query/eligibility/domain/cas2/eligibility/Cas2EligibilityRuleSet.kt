package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas2EligibilityRuleSet(
  under18: Under18Rule,
  currentAccommodationTypeIsCas3: CurrentAccommodationTypeIsCas3Rule,
  currentAccommodationTypeIsCas2: CurrentAccommodationTypeIsCas2Rule,
  currentAccommodationTypeIsCommunity: CurrentAccommodationTypeIsCommunityRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    under18,
    currentAccommodationTypeIsCas3,
    currentAccommodationTypeIsCas2,
    currentAccommodationTypeIsCommunity,
  )

  override fun getRules(): List<Rule> = rules
}
