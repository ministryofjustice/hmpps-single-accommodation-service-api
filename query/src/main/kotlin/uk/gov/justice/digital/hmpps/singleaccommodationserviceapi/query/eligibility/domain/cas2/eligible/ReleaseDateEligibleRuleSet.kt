package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.InPrisonNoReleaseRule

@Component
class ReleaseDateEligibleRuleSet(
  inPrisonNoRelease: InPrisonNoReleaseRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    inPrisonNoRelease,
  )

  override val name: String = "Release Date Eligible"

  override fun getRules(): List<Rule> = rules
}
