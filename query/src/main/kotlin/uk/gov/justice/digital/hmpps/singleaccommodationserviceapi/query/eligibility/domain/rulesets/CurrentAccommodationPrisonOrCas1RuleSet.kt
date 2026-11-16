package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CurrentAccommodationNotPrisonOrCas1Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class CurrentAccommodationPrisonOrCas1RuleSet(
  currentAccommodationNotPrisonOrCas1: CurrentAccommodationNotPrisonOrCas1Rule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    currentAccommodationNotPrisonOrCas1,
  )

  override val name: String = "PASS if the person is in prison or CAS1"

  override fun getRules(): List<Rule> = rules
}
