package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isLessThanXWeeksInTheFuture
import java.time.Clock
import java.time.LocalDate

@Component
class ReleaseOutside12WeeksRule(val clock: Clock) : Rule {
  override val description = "FAIL if release is more than 12 weeks from today"

  override fun evaluate(data: DomainData): RuleResult {
    val today = LocalDate.now(clock)
    return RuleResult(
      description = description,
      ruleStatus = if (isLessThanXWeeksInTheFuture(
          endDate = data.currentAccommodation?.endDate,
          today = today,
          numOfWeeks = 12L,
        )
      ) {
        RuleStatus.PASS
      } else {
        RuleStatus.FAIL
      },
    )
  }
}
