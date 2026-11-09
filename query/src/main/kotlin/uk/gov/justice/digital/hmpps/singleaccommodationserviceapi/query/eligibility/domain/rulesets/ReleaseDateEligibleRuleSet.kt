package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.InPrisonNoReleaseRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class ReleaseDateEligibleRuleSet(
  inPrisonNoRelease: InPrisonNoReleaseRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    inPrisonNoRelease,
  )

  override val name: String = "PASS if person is in prison and has a release date or is in other accommodation"

  override fun getRules(): List<Rule> = rules
}
