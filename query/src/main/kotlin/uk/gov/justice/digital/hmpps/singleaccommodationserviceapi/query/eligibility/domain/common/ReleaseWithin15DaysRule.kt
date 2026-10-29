package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isLessThan15DaysInTheFuture
import java.time.Clock
import java.time.LocalDate

@Component
class ReleaseWithin15DaysRule(val clock: Clock) : Rule {
  override val description = "FAIL if release is more than 15 days from today"

  override fun evaluate(data: DomainData): RuleResult {
    val today = LocalDate.now(clock)

    return RuleResult(
      description = description,
      ruleStatus = if (isLessThan15DaysInTheFuture(
          endDate = data.currentAccommodation?.endDate,
          today = today,
        )
      ) {
        RuleStatus.PASS
      } else {
        RuleStatus.FAIL
      },
    )
  }
}
