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
  override val description = "FAIL if CRS not submitted"
  override val reasonOnRuleFailure = FailureReason.CRS_NOT_SUBMITTED
  fun isFail(status: CrsReferralStatus?) = status !in SUBMITTED_STATUSES
  lateinit var data: DomainData
  override fun evaluate(data: DomainData): RuleResult {
    this.data = data
    val status = data.commissionedRehabilitativeServices?.status
    val isFail = status !in SUBMITTED_STATUSES

    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) reasonOnRuleFailure else null,
    )
  }

  companion object {
    private val SUBMITTED_STATUSES = setOf(
      CrsReferralStatus.LIVE,
      CrsReferralStatus.COMPLETED,
    )
  }
}

@Component
class CrsSubmittedRuleMale : CrsSubmittedRule() {
  override val description = super.description + " and male"
  override val reasonOnRuleFailure = FailureReason.CRS_NOT_SUBMITTED_MALE
  fun isMale(sexCode: SexCode?) = sexCode === SexCode.M
  override fun isFail(status: CrsReferralStatus?) = super.isFail(status) && isMale(data.sex)
}

@Component
class CrsSubmittedRuleNonMale : CrsSubmittedRule() {
  override val description = super.description + " and non-male"
  override val reasonOnRuleFailure = FailureReason.CRS_NOT_SUBMITTED_NON_MALE
  fun isMale(sexCode: SexCode?) = sexCode === SexCode.M
  override fun isFail(status: CrsReferralStatus?) = super.isFail(status) && !isMale(data.sex)
}
