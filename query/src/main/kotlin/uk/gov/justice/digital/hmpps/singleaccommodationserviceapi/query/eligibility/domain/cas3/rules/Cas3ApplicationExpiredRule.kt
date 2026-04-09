package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class Cas3ApplicationExpiredRule : Cas3SuitabilityRule {
  override val description = "FAIL if CAS3 application is arrived, cancelled or closed"

  override fun evaluate(data: DomainData): RuleResult {
    val expiredStatuses = listOf(
      Cas3PlacementStatus.ARRIVED,
      Cas3PlacementStatus.DEPARTED,
      Cas3PlacementStatus.CLOSED,
    )

    val ruleStatus = if (expiredStatuses.contains(data.cas3Application?.bookingStatus)) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
