package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class CrsSubmittedRuleNonMale : Rule {
  override val description = "FAIL if CRS not submitted and not male"

  override fun evaluate(data: DomainData): RuleResult {
    val status = data.commissionedRehabilitativeServices?.status
    val isFail = status !in SUBMITTED_STATUSES && data.sex !== SexCode.M
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) FailureReason.CRS_NOT_SUBMITTED_NON_MALE else null,
    )
  }

  companion object {
    private val SUBMITTED_STATUSES = setOf(
      CrsReferralStatus.LIVE,
      CrsReferralStatus.COMPLETED,
    )
  }
}
