package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.InPrisonNoReleaseRule

@Component
class ReleaseDateEligibleRuleSet(
  inPrisonNoReleaseDate: InPrisonNoReleaseRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(inPrisonNoReleaseDate, 1),
  )

  override val name: String = "Release Date Eligible"

  override fun getRules(): List<PrioritisedRule> = rules
}
