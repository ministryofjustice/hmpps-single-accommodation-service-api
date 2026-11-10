package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.InPrisonNoReleaseRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.AccommodationData

@Component
class ReleaseDateEligibleRuleSet(
  inPrisonNoRelease: InPrisonNoReleaseRule,
) : RuleSet<AccommodationData> {
  private val rules: List<Rule<AccommodationData>> = listOf(
    inPrisonNoRelease,
  )

  override val name: String = "Release Date Eligible"

  override fun getRules(): List<Rule<AccommodationData>> = rules
}
