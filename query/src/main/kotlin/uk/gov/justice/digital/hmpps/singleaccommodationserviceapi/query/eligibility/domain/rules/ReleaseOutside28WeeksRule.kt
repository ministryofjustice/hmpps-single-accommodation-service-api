package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.CAS1_UPCOMING_WEEKS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.isLessThanXWeeksInTheFuture
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.CurrentAccommodationData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField
import java.time.Clock
import java.time.LocalDate

@Component
class ReleaseOutside28WeeksRule(val clock: Clock) : Rule<CurrentAccommodationData> {
  override val inputFields = setOf(DomainDataField.CURRENT_ACCOMMODATION_END_DATE)
  override val description = "FAIL if release is more than 28 weeks from today"

  override fun evaluate(data: CurrentAccommodationData): RuleResult {
    val today = LocalDate.now(clock)

    return RuleResult(
      description = description,
      ruleStatus = if (isLessThanXWeeksInTheFuture(
          endDate = data.currentAccommodation?.endDate,
          today = today,
          numOfWeeks = CAS1_UPCOMING_WEEKS,
        )
      ) {
        RuleStatus.PASS
      } else {
        RuleStatus.FAIL
      },
    )
  }
}
