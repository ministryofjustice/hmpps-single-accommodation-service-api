package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class MaleRiskEligibilityRule : Cas1EligibilityRule {
  override val description = "FAIL if candidate is Male and is not Tier A3 - B1"
  private val highRiskTiers = listOf(
    TierScore.A3,
    TierScore.A2,
    TierScore.A1,
    TierScore.A3S,
    TierScore.A2S,
    TierScore.A1S,
    TierScore.B3,
    TierScore.B2,
    TierScore.B1,
    TierScore.B3S,
    TierScore.B2S,
    TierScore.B1S,
  )
  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.sex.code == SexCode.M && !highRiskTiers.contains(data.tier)) {
      RuleStatus.FAIL
    } else {
      RuleStatus.PASS
    },
    actionable = this.actionable,
  )
}
