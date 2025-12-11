package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.RuleStatus

class STierRule : Rule {
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
  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (sRiskTiers.contains(data.tier)) {
      RuleStatus.FAIL
    } else {
      RuleStatus.PASS
    },
    actionable = this.actionable,
  )
}
