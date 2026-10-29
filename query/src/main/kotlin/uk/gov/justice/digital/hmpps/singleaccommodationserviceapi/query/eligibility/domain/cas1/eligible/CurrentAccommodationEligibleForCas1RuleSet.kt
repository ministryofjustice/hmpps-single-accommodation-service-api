package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.rules.CurrentAccommodationIsPrisonOrCas1Rule

@Component
class CurrentAccommodationEligibleForCas1RuleSet(
  currentAccommodation: CurrentAccommodationIsPrisonOrCas1Rule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(currentAccommodation, 1),
  )

  override val name: String = "Eligibility Current Accommodation"

  override fun getRules(): List<PrioritisedRule> = rules
}
