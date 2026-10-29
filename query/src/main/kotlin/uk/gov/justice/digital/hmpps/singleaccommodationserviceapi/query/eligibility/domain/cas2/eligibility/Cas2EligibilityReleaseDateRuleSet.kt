package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas2EligibilityReleaseDateRuleSet(
  inPrisonNoReleaseDate: InPrisonNoReleaseDateRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(inPrisonNoReleaseDate, 1),
  )

  override val name: String = "Eligibility Release Date"

  override fun getRules(): List<PrioritisedRule> = rules
}
