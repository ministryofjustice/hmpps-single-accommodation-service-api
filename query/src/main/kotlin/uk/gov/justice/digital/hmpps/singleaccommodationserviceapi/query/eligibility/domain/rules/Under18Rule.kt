package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DateOfBirthData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField
import java.time.Clock
import java.time.LocalDate

@Component
class Under18Rule(val clock: Clock) : Rule<DateOfBirthData> {
  override val inputFields = setOf(DomainDataField.DATE_OF_BIRTH)
  override val description = "FAIL if person is under 18 years old"
  override val failureReason = FailureReason.UNDER_18

  override fun evaluate(data: DateOfBirthData): RuleResult {
    val eighteenYearsAgo = LocalDate.now(clock).minusYears(18)
    val isFail = data.dob?.isAfter(eighteenYearsAgo) ?: false
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) failureReason else null,
    )
  }
}
