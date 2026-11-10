package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.isDtrExpired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DtrReferralData
import java.time.Clock

@Component
class DtrExpiredRule(val clock: Clock) : Rule<DtrReferralData> {
  override val inputFields = setOf(DomainDataField.DTR_SUBMISSION_DATE)
  override val description = "FAIL if the DTR submission date is missing or more than 26 weeks ago"
  override val failureReason = FailureReason.DTR_REFERRAL_EXPIRED

  override fun evaluate(data: DtrReferralData): RuleResult {
    val isFail = isDtrExpired(data.dutyToRefer?.submission?.submissionDate, clock)
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) failureReason else null,
    )
  }
}
