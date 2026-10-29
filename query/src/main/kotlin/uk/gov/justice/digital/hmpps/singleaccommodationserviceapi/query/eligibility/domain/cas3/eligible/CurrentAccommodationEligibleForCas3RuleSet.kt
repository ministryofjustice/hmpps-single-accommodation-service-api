package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligible

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligible.rules.WrongCurrentAccommodationForCas3TypeRule

@Component
class CurrentAccommodationEligibleForCas3RuleSet(
  currentAccommodationType: WrongCurrentAccommodationForCas3TypeRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(currentAccommodationType, 0),
  )

  override val name: String = "Eligibility Current Accommodation"

  override fun getRules(): List<PrioritisedRule> = rules
}
