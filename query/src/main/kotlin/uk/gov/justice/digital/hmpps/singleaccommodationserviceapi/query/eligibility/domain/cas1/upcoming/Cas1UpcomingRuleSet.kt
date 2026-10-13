package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

@Component
class Cas1UpcomingRuleSet(
  releaseWithinOneYear: ReleaseWithinOneYearRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(releaseWithinOneYear)
  override fun getRules(): List<Rule> = rules
}
