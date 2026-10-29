package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import java.time.Clock
import java.time.LocalDate

@Component
class Under18Rule(val clock: Clock) : Rule {
  override val description = "FAIL if individual is under 18 years old"

  override fun evaluate(data: DomainData): RuleResult {
    val eighteenYearsAgo = LocalDate.now(clock).minusYears(18)
    val isFail = data.dob?.isAfter(eighteenYearsAgo) ?: false
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) FailureReason.UNDER_18 else null,
    )
  }
}
