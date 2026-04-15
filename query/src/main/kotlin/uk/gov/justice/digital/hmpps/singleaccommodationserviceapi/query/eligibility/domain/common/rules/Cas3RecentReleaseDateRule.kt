package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.completion.Cas3CompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.suitability.Cas3SuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isWithinOneYear
import java.time.Clock
import java.time.LocalDate

@Component
class Cas3RecentReleaseDateRule(val clock: Clock) :
  Cas3SuitabilityRule,
  Cas3CompletionRule {
  override val description = "FAIL if CAS3 application is not within 1 year of release"

  override fun evaluate(data: DomainData): RuleResult {
    val today = LocalDate.now(clock)
    val isWithinOneYear = isWithinOneYear(
      releaseDate = data.releaseDate,
      today = today,
    )

    return RuleResult(
      description = description,
      ruleStatus = if (isWithinOneYear) RuleStatus.PASS else RuleStatus.FAIL,
    )
  }
}
