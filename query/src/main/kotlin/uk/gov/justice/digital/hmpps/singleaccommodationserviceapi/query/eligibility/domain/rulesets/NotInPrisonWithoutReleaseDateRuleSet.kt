package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.InPrisonNoReleaseDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class NotInPrisonWithoutReleaseDateRuleSet(
  inPrisonNoReleaseDate: InPrisonNoReleaseDateRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    inPrisonNoReleaseDate,
  )

  override val name: String = "PASS if the person is not in prison, or is in prison with no release date"

  override fun getRules(): List<Rule> = rules
}
