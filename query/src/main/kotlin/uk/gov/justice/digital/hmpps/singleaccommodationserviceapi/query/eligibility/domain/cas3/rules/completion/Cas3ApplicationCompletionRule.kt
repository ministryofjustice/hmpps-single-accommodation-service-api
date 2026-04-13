package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

private val completedBookingStatuses = listOf(
  Cas3BookingStatus.PROVISIONAL,
  Cas3BookingStatus.CONFIRMED,
  Cas3BookingStatus.ARRIVED,
)

@Component
class Cas3ApplicationCompletionRule : Cas3CompletionRule {
  override val description = "FAIL if CAS3 application is not complete"

  override fun evaluate(data: DomainData): RuleResult {
    val isCompleteApplication = data.cas3Application?.applicationStatus == Cas3ApplicationStatus.PLACED
    val isCompleteBooking = data.cas3Application?.bookingStatus in completedBookingStatuses

    val ruleStatus = if (isCompleteApplication && isCompleteBooking) RuleStatus.PASS else RuleStatus.FAIL

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
