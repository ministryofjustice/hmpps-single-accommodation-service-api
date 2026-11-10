package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.WrongCurrentAccommodationForCas3TypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.AccommodationTypeData

@Component
class CurrentAccommodationEligibleForCas3RuleSet(
  wrongCurrentAccommodationForCas3Type: WrongCurrentAccommodationForCas3TypeRule,
) : RuleSet<AccommodationTypeData> {
  private val rules: List<Rule<AccommodationTypeData>> = listOf(
    wrongCurrentAccommodationForCas3Type,
  )

  override val name: String = "Current Accommodation Eligible For CAS3"

  override fun getRules(): List<Rule<AccommodationTypeData>> = rules
}
