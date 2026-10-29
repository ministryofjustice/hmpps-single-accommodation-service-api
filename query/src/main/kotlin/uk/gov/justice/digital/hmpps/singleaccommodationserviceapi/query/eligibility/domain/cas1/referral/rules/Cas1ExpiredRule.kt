package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class Cas1ExpiredRule : Rule {
  override val description = "FAIL if the application is expired and its placement status is neither upcoming nor arrived"

  override fun evaluate(data: DomainData): RuleResult {
    val isFail = data.cas1Application?.application?.status == Cas1ApplicationStatus.EXPIRED &&
      (data.cas1Application.placement?.status != Cas1PlacementStatus.UPCOMING && data.cas1Application.placement?.status != Cas1PlacementStatus.ARRIVED)

    val ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
