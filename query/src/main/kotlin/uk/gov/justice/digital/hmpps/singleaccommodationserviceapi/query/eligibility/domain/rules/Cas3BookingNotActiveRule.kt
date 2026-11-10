package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas3ApplicationData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class Cas3BookingNotActiveRule : Rule<Cas3ApplicationData> {
  override val inputFields = setOf(DomainDataField.CAS3_BOOKING_STATUS)
  override val description = "FAIL if the booking is departed or closed"

  override fun evaluate(data: Cas3ApplicationData): RuleResult {
    val expiredStatuses = listOf(
      Cas3BookingStatus.DEPARTED,
      Cas3BookingStatus.CLOSED,
    )

    val ruleStatus = if (data.cas3Application?.submittedApplication?.latestBooking?.status in expiredStatuses) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
