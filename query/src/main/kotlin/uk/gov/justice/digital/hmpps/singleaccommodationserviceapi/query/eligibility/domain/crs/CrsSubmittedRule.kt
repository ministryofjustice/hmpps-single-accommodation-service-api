package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class CrsSubmittedRule : Rule {
  override val description = "FAIL if CRS not submitted"

  override fun evaluate(data: DomainData): RuleResult {
    val status = data.commissionedRehabilitativeServices?.status
    val isFail = status !in SUBMITTED_STATUSES
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) FailureReason.CRS_NOT_SUBMITTED else null,
    )
  }

  companion object {
    private val SUBMITTED_STATUSES = setOf(
      CrsReferralStatus.ACTION_PLAN_SUBMITTED,
      CrsReferralStatus.ACTION_PLAN_APPROVED,
      CrsReferralStatus.APPOINTMENT,
      CrsReferralStatus.COMPLETED,
      CrsReferralStatus.NSI_COMMENCED,
      CrsReferralStatus.END_OF_SERVICE_REPORT,
    )
  }
}
