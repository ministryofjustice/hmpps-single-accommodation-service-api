package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
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
    val isFail = !isLessThanXWeeksInThePast(data.commissionedRehabilitativeServices?.submissionDate, today, 12L)
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) FailureReason.CRS_EXPIRED else null,
    )
  }
}
