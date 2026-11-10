package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CurrentAccommodationIsPrisonOrCas1Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.AccommodationTypeData

@Component
class CurrentAccommodationEligibleForCas1RuleSet(
  currentAccommodationIsPrisonOrCas1: CurrentAccommodationIsPrisonOrCas1Rule,
) : RuleSet<AccommodationTypeData> {
  private val rules: List<Rule<AccommodationTypeData>> = listOf(
    currentAccommodationIsPrisonOrCas1,
  )

  override val name: String = "Current Accommodation Eligible For CAS1"

  override fun getRules(): List<Rule<AccommodationTypeData>> = rules
}
