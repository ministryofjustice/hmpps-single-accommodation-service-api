package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isLessThanXWeeksInTheFuture
import java.time.Clock
import java.time.LocalDate

@Component
class ReleaseWithinFourWeeksRule(val clock: Clock) : Rule {
  override val description = "FAIL if not within 4 weeks of release from current accommodation"

  override fun evaluate(data: DomainData): RuleResult {
    val today = LocalDate.now(clock)

    return RuleResult(
      description = description,
      ruleStatus = if (isLessThanXWeeksInTheFuture(
          endDate = data.currentAccommodation?.endDate,
          today = today,
          numOfWeeks = 4L,
        )
      ) {
        RuleStatus.PASS
      } else {
        RuleStatus.FAIL
      },
    )
  }
}
