package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas3CompletionContextUpdater : ContextUpdater() {

  override val description = set("status from booking")

  val bedspaceOffered = "bedspaceOffered"
  val bookingConfirmed = "bookingConfirmed"
  val notArrived = "notArrived"
  val bedspaceCancelled = "bedspaceCancelled"
  val submitted = "submitted"

  override val outcomes = mapOf(
    bedspaceOffered to ServiceResult(
      serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
      action = CaseAction(type = CaseActionType.REPLY_TO_CAS3_BEDSPACE_OFFER, service = AccommodationService.CAS3),
      link = EligibilityKeys.VIEW_REFERRAL,
      linkType = LinkType.CAS3_VIEW_REFERRAL,
    ),
    bookingConfirmed to ServiceResult(
      serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
      link = EligibilityKeys.VIEW_REFERRAL,
      linkType = LinkType.CAS3_VIEW_REFERRAL,
    ),
    notArrived to ServiceResult(
      serviceStatus = ServiceStatus.NOT_ARRIVED,
      link = EligibilityKeys.VIEW_REFERRAL,
      linkType = LinkType.CAS3_VIEW_REFERRAL,
    ),
    bedspaceCancelled to ServiceResult(
      serviceStatus = ServiceStatus.BOOKING_CANCELLED,
      link = EligibilityKeys.VIEW_REFERRAL,
      linkType = LinkType.CAS3_VIEW_REFERRAL,
    ),
    submitted to ServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      link = EligibilityKeys.VIEW_REFERRAL,
      linkType = LinkType.CAS3_VIEW_REFERRAL,
    ),
  )

  override fun toServiceResult(context: EvaluationContext): ServiceResult {
    val bookingStatus = context.data.cas3Application?.bookingStatus
    return when (bookingStatus) {
      Cas3BookingStatus.PROVISIONAL -> outcome(bedspaceOffered)
      Cas3BookingStatus.CONFIRMED -> outcome(bookingConfirmed)
      Cas3BookingStatus.NOT_MINUS_ARRIVED -> outcome(notArrived)
      Cas3BookingStatus.CANCELLED -> outcome(bedspaceCancelled)
      else -> outcome(submitted)
    }
  }
}
