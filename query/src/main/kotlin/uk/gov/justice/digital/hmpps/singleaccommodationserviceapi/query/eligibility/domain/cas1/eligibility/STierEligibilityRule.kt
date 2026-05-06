package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class STierEligibilityRule : Cas1EligibilityRule {
  override val description = "FAIL if candidate is S Tier"
  private val sRiskTiers = listOf(
    TierScore.A3S,
    TierScore.A2S,
    TierScore.A1S,
    TierScore.B3S,
    TierScore.B2S,
    TierScore.B1S,
    TierScore.C3S,
    TierScore.C2S,
    TierScore.C1S,
    TierScore.D3S,
    TierScore.D2S,
    TierScore.D1S,
  )

  override fun evaluate(data: DomainData): RuleResult {
    val isFail = sRiskTiers.contains(data.tierScore)
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) FailureReason.S_TIER else null,
    )
  }
}
