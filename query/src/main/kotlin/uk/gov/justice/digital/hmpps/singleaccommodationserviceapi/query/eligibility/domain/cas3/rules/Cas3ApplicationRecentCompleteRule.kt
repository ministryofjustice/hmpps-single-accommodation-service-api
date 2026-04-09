package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import java.time.Clock
import java.time.LocalDate

@Component
class Cas3ApplicationRecentCompleteRule(val clock: Clock) : Cas3CompletionRule {
  override val description = "FAIL if CAS3 application is not within 1 year of release"

  override fun evaluate(data: DomainData): RuleResult {
    val releaseDate = data.releaseDate ?: return RuleResult(
      description = description,
      ruleStatus = RuleStatus.FAIL,
    )

    val today = LocalDate.now(clock)
    val isWithinOneYear = !releaseDate.isAfter(today.plusYears(1))

    val ruleStatus = if (isWithinOneYear) RuleStatus.PASS else RuleStatus.FAIL

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
