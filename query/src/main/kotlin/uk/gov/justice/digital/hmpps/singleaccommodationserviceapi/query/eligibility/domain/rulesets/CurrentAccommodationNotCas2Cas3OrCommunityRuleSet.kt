package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CurrentAccommodationTypeIsCas2Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CurrentAccommodationTypeIsCas3Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CurrentAccommodationTypeIsCommunityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class CurrentAccommodationNotCas2Cas3OrCommunityRuleSet(
  currentAccommodationTypeIsCas3: CurrentAccommodationTypeIsCas3Rule,
  currentAccommodationTypeIsCas2: CurrentAccommodationTypeIsCas2Rule,
  currentAccommodationTypeIsCommunity: CurrentAccommodationTypeIsCommunityRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    currentAccommodationTypeIsCas3,
    currentAccommodationTypeIsCas2,
    currentAccommodationTypeIsCommunity,
  )

  override val name: String = "PASS if the person is not in CAS2, CAS3 or the community"

  override fun getRules(): List<Rule> = rules
}
