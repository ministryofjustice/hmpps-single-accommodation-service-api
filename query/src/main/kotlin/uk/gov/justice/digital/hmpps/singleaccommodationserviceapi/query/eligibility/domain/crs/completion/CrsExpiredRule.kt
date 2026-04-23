package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isWithin12weeksInThePast
import java.time.Clock
import java.time.LocalDate

@Component
class CrsExpiredRule(val clock: Clock) : CrsCompletionRule {
  override val description = "FAIL if CRS not within 12 weeks"

  override fun evaluate(data: DomainData): RuleResult {
    val today = LocalDate.now(clock)

    return RuleResult(
      description = description,
      ruleStatus = if (
        isWithin12weeksInThePast(data.commissionedRehabilitativeServices?.submissionDate, today)
      ) {
        RuleStatus.PASS
      } else {
        RuleStatus.FAIL
      },
    )
  }
}
