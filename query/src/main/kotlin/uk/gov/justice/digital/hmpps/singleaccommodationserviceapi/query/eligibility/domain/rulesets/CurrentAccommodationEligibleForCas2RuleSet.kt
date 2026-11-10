package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CurrentAccommodationTypeIsCas2Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CurrentAccommodationTypeIsCas3Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CurrentAccommodationTypeIsCommunityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.AccommodationTypeData

@Component
class CurrentAccommodationEligibleForCas2RuleSet(
  currentAccommodationTypeIsCas3: CurrentAccommodationTypeIsCas3Rule,
  currentAccommodationTypeIsCas2: CurrentAccommodationTypeIsCas2Rule,
  currentAccommodationTypeIsCommunity: CurrentAccommodationTypeIsCommunityRule,
) : RuleSet<AccommodationTypeData> {
  private val rules: List<Rule<AccommodationTypeData>> = listOf(
    currentAccommodationTypeIsCas3,
    currentAccommodationTypeIsCas2,
    currentAccommodationTypeIsCommunity,
  )

  override val name: String = "Current Accommodation Eligible For CAS2"

  override fun getRules(): List<Rule<AccommodationTypeData>> = rules
}
