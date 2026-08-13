package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
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
  private var useSexCode = false

  override fun evaluate(data: DomainData): RuleResult {
    val today = LocalDate.now(clock)
    val isFail = !isLessThanXWeeksInThePast(data.commissionedRehabilitativeServices?.sentAt?.toLocalDate(), today, 12L)
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) failureReason(data.sex) else null,
    )
  }
  fun failureReason(sexCode: SexCode?): FailureReason = if (!useSexCode) {
    FailureReason.CRS_EXPIRED
  } else {
    if (sexCode == SexCode.M) {
      FailureReason.CRS_EXPIRED_MALE
    } else {
      FailureReason.CRS_EXPIRED_NON_MALE
    }
  }
  fun withSexCode(): CrsExpiredRule {
    useSexCode = true
    return this
  }
}
