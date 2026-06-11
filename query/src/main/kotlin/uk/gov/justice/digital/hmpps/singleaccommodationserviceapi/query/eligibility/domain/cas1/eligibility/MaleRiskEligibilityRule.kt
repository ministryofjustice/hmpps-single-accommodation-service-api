package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class MaleRiskEligibilityRule : Rule {
  override val description = "FAIL if candidate is Male and is not Tier A3 - B1"

  override fun evaluate(data: DomainData): RuleResult {
    val isFail = data.sex == SexCode.M && !TierRiskScores.isHighRiskForMale(data.tierScore)
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) FailureReason.MALE_NOT_HIGH_RISK_TIER else null,
    )
  }
}
