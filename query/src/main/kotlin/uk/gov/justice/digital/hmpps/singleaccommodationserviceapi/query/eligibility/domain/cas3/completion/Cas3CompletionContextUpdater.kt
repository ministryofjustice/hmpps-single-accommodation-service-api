package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas3CompletionContextUpdater(
  @Value($$"${service.temporary-accommodation-ui.base-url}") temporaryAccommodationUiBaseUrl: String,
) : ContextUpdater() {

  val url = temporaryAccommodationUiBaseUrl

  override fun toServiceResult(context: EvaluationContext): ServiceResult {
    val assessmentStatus = context.data.cas3Application?.assessmentStatus
    val bookingStatus = context.data.cas3Application?.bookingStatus
    return when (bookingStatus) {
      Cas3BookingStatus.PROVISIONAL -> ServiceResult(
        serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
        action = EligibilityKeys.REPLY_TO_CAS3_BEDSPACE_OFFER,
        link = EligibilityKeys.VIEW_REFERRAL,
        url = url,
      )

      Cas3BookingStatus.CONFIRMED -> ServiceResult(
        serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
        link = EligibilityKeys.VIEW_REFERRAL,
        url = url,
      )

      Cas3BookingStatus.NOT_MINUS_ARRIVED -> ServiceResult(
        serviceStatus = ServiceStatus.NOT_ARRIVED,
        link = EligibilityKeys.VIEW_REFERRAL,
        url = url,
      )

      Cas3BookingStatus.CANCELLED -> ServiceResult(
        serviceStatus = ServiceStatus.BOOKING_CANCELLED,
        link = EligibilityKeys.VIEW_REFERRAL,
        url = url,
      )

      else -> when (assessmentStatus) {
        Cas3AssessmentStatus.UNALLOCATED,
        Cas3AssessmentStatus.IN_REVIEW,
        Cas3AssessmentStatus.READY_TO_PLACE,
        -> ServiceResult(
          serviceStatus = ServiceStatus.SUBMITTED,
          link = EligibilityKeys.VIEW_REFERRAL,
          url = url,
        )

        else -> ServiceResult(
          serviceStatus = ServiceStatus.SUBMITTED,
          link = EligibilityKeys.VIEW_REFERRAL,
          url = url,
        )
      }
    }
  }
}
