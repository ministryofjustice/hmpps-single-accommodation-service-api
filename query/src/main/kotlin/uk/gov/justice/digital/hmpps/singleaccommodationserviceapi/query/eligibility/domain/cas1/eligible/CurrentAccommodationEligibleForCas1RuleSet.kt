package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.rules.CurrentAccommodationIsPrisonOrCas1Rule

@Component
class CurrentAccommodationEligibleForCas1RuleSet(
  currentAccommodationIsPrisonOrCas1: CurrentAccommodationIsPrisonOrCas1Rule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    currentAccommodationIsPrisonOrCas1,
  )

  override val name: String = "Current Accommodation Eligible For CAS1"

  override fun getRules(): List<Rule> = rules
}
