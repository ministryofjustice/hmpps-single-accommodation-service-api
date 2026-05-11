package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isLessThanOneYearInTheFuture
import java.time.Clock
import java.time.LocalDate

@Component
class RecentCurrentAccommodationEndDateRule(val clock: Clock) :
  Cas1UpcomingRule,
  Cas3UpcomingRule {
  override val description = "FAIL if not within 1 year of release from current accommodation"

  override fun evaluate(data: DomainData): RuleResult {
    val today = LocalDate.now(clock)
    val isFail = !isLessThanOneYearInTheFuture(endDate = data.currentAccommodation?.endDate, today = today)
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
    )
  }
}
