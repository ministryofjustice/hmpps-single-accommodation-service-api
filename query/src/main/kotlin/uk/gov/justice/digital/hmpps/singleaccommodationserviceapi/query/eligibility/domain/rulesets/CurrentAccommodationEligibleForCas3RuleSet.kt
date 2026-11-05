package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.WrongCurrentAccommodationForCas3TypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class CurrentAccommodationEligibleForCas3RuleSet(
  wrongCurrentAccommodationForCas3Type: WrongCurrentAccommodationForCas3TypeRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    wrongCurrentAccommodationForCas3Type,
  )

  override val name: String = "Current Accommodation Eligible For CAS3"

  override fun getRules(): List<Rule> = rules
}
