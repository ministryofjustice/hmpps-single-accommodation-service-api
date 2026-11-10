package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.isLessThanXWeeksInTheFuture
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData
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
