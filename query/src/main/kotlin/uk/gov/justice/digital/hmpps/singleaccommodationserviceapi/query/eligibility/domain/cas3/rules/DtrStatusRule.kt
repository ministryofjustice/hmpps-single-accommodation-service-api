package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class DtrStatusRule : Cas3EligibilityRule {
  override val description = "FAIL if DTR status is not submitted"

  override fun evaluate(data: DomainData): RuleResult {
    // TODO: Replace with actual DTR status check when available
    val isEligible = data.dtrStatus == "submitted"

    return RuleResult(
      description = description,
      ruleStatus = if (isEligible) RuleStatus.PASS else RuleStatus.FAIL,
    )
  }
}
