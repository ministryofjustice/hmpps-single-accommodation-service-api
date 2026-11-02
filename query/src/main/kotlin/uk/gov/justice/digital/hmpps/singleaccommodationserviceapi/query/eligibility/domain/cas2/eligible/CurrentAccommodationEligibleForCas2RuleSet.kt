package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.CurrentAccommodationTypeIsCas2Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.CurrentAccommodationTypeIsCas3Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.CurrentAccommodationTypeIsCommunityRule

@Component
class CurrentAccommodationEligibleForCas2RuleSet(
  currentAccommodationTypeIsCas3: CurrentAccommodationTypeIsCas3Rule,
  currentAccommodationTypeIsCas2: CurrentAccommodationTypeIsCas2Rule,
  currentAccommodationTypeIsCommunity: CurrentAccommodationTypeIsCommunityRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    currentAccommodationTypeIsCas3,
    currentAccommodationTypeIsCas2,
    currentAccommodationTypeIsCommunity,
  )

  override val name: String = "Current Accommodation Eligible For CAS2"

  override fun getRules(): List<Rule> = rules
}
