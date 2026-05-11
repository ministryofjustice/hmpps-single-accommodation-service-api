package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isLessThanXWeeksInThePast
import java.time.Clock
import java.time.LocalDate

@Component
class CrsExpiredRule(val clock: Clock) : Rule {
  override val description = "FAIL if CRS not within 12 weeks"

  override fun evaluate(data: DomainData): RuleResult {
    val today = LocalDate.now(clock)

    return RuleResult(
      description = description,
      ruleStatus = if (
        isLessThanXWeeksInThePast(data.commissionedRehabilitativeServices?.submissionDate, today, 12L)
      ) {
        RuleStatus.PASS
      } else {
        RuleStatus.FAIL
      },
    )
  }
}
