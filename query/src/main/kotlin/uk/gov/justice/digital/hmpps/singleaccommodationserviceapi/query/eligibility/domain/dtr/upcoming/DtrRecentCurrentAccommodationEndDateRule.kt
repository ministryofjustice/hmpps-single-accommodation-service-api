package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isLessThan56DaysInTheFuture
import java.time.Clock
import java.time.LocalDate

@Component
class DtrRecentCurrentAccommodationEndDateRule(val clock: Clock) : Rule {
  override val description = "FAIL if not within 56 days of release from current accommodation"

  override fun evaluate(data: DomainData): RuleResult {
    val today = LocalDate.now(clock)

    return RuleResult(
      description = description,
      ruleStatus = if (isLessThan56DaysInTheFuture(
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
