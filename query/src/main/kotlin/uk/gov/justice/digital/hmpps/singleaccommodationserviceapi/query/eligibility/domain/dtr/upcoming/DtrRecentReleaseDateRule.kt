package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isWithin56Days
import java.time.Clock
import java.time.LocalDate

@Component
class DtrRecentReleaseDateRule(val clock: Clock) : DtrUpcomingRule {
  override val description = "FAIL if not within 56 days of release date from prison when current accommodation is prison"

  override fun evaluate(data: DomainData): RuleResult {
    val today = LocalDate.now(clock)

    val releaseDate = if (data.currentAccommodationArrangementType == AccommodationArrangementType.PRISON && data.currentAccommodationEndDate == null) {
      data.releaseDate
    } else {
      null
    }

    return RuleResult(
      description = description,
      ruleStatus = if (isWithin56Days(
          releaseDate = releaseDate,
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
