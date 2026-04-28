package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class Cas3BookingSuitabilityRule : Cas3SuitabilityRule {
  override val description = "FAIL if booking is arrived, departed or closed"

  override fun evaluate(data: DomainData): RuleResult {
    val expiredStatuses = listOf(
      Cas3BookingStatus.ARRIVED,
      Cas3BookingStatus.DEPARTED,
      Cas3BookingStatus.CLOSED,
    )

    val ruleStatus = if (data.cas3Application?.bookingStatus in expiredStatuses) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
