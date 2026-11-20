package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CurrentAccommodationNotPrisonCas1OrCas2Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class CurrentAccommodationPrisonCas1OrCas2RuleSet(
  currentAccommodationNotPrisonCas1OrCas2: CurrentAccommodationNotPrisonCas1OrCas2Rule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    currentAccommodationNotPrisonCas1OrCas2,
  )

  override val name: String = "PASS if the person is in CAS1, CAS2 or prison"

  override fun getRules(): List<Rule> = rules
}
