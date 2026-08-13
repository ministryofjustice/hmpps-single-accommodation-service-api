package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import kotlin.collections.contains

@Component
class CrsSubmittedRule : Rule {
  private var useSexCode = false

  override val description = "FAIL if CRS not submitted"

  override fun evaluate(data: DomainData): RuleResult {
    val status = data.commissionedRehabilitativeServices?.status
    val isFail = status !in SUBMITTED_STATUSES
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) failureReason(data.sex) else null,
    )
  }
  fun failureReason(sexCode: SexCode?): FailureReason = if (!useSexCode) {
    FailureReason.CRS_NOT_SUBMITTED
  } else {
    if (sexCode == SexCode.M) {
      FailureReason.CRS_NOT_SUBMITTED_MALE
    } else {
      FailureReason.CRS_NOT_SUBMITTED_NON_MALE
    }
  }
  fun withSexCode(): CrsSubmittedRule {
    useSexCode = true
    return this
  }
  companion object {
    private val SUBMITTED_STATUSES = setOf(
      CrsReferralStatus.LIVE,
      CrsReferralStatus.COMPLETED,
    )
  }
}
