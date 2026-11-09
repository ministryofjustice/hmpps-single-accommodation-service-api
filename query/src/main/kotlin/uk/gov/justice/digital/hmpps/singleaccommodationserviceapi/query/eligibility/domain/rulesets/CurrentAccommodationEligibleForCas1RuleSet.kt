package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CurrentAccommodationIsPrisonOrCas1Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class CurrentAccommodationEligibleForCas1RuleSet(
  currentAccommodationIsPrisonOrCas1: CurrentAccommodationIsPrisonOrCas1Rule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    currentAccommodationIsPrisonOrCas1,
  )

  override val name: String = "PASS if person is in prison or CAS1"

  override fun getRules(): List<Rule> = rules
}
