package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.CrsRequirementData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class CrsSubmittedRuleNonMale : Rule<CrsRequirementData> {
  override val inputFields = setOf(DomainDataField.CRS_STATUS, DomainDataField.SEX)
  override val description = "FAIL if person is not male and the referral is neither live nor completed"
  override val failureReason = FailureReason.CRS_NOT_SUBMITTED_NON_MALE

  override fun evaluate(data: CrsRequirementData): RuleResult {
    val status = data.commissionedRehabilitativeServices?.status
    val isFail = status !in SUBMITTED_STATUSES && data.sex !== SexCode.M
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) failureReason else null,
    )
  }

  companion object {
    private val SUBMITTED_STATUSES = setOf(
      CrsReferralStatus.LIVE,
      CrsReferralStatus.COMPLETED,
    )
  }
}
