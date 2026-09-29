package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.ApprovedPremisesApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.Cas1SpaceBookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.Cas3BookingStatus

object AccommodationReferralStatusMapper {
  fun toStatus(referral: Cas1ReferralHistory): AccommodationReferralStatus = when (referral.placementStatus) {
    Cas1SpaceBookingStatus.NOT_ARRIVED -> AccommodationReferralStatus.NOT_ARRIVED
    Cas1SpaceBookingStatus.DEPARTED -> AccommodationReferralStatus.DEPARTED
    Cas1SpaceBookingStatus.CANCELLED -> AccommodationReferralStatus.CANCELLED
    Cas1SpaceBookingStatus.ARRIVED,
    Cas1SpaceBookingStatus.UPCOMING,
    -> AccommodationReferralStatus.ACCEPTED
    null -> when (referral.requestForPlacementStatus) {
      RequestForPlacementStatus.REQUEST_REJECTED -> AccommodationReferralStatus.REQUEST_REJECTED
      RequestForPlacementStatus.REQUEST_WITHDRAWN -> AccommodationReferralStatus.REQUEST_WITHDRAWN
      RequestForPlacementStatus.PLACEMENT_BOOKED -> AccommodationReferralStatus.ACCEPTED
      RequestForPlacementStatus.REQUEST_UNSUBMITTED,
      RequestForPlacementStatus.REQUEST_SUBMITTED,
      RequestForPlacementStatus.AWAITING_MATCH,
      null,
      -> when (referral.applicationStatus) {
        ApprovedPremisesApplicationStatus.EXPIRED -> AccommodationReferralStatus.EXPIRED
        ApprovedPremisesApplicationStatus.WITHDRAWN -> AccommodationReferralStatus.WITHDRAWN
        ApprovedPremisesApplicationStatus.PLACEMENT_ALLOCATED -> AccommodationReferralStatus.ACCEPTED
        ApprovedPremisesApplicationStatus.REJECTED,
        ApprovedPremisesApplicationStatus.INAPPLICABLE,
        -> AccommodationReferralStatus.REJECTED
        else -> AccommodationReferralStatus.PENDING
      }
    }
  }

  fun toStatus(referral: Cas2ReferralHistory): AccommodationReferralStatus = when (referral.applicationStatus) {
    Cas2AssessmentStatus.MORE_INFO_REQUESTED -> AccommodationReferralStatus.MORE_INFORMATION_REQUESTED
    Cas2AssessmentStatus.PLACE_OFFERED -> AccommodationReferralStatus.PLACE_OFFERED
    Cas2AssessmentStatus.AWAITING_ARRIVAL -> AccommodationReferralStatus.AWAITING_ARRIVAL
    Cas2AssessmentStatus.CANCELLED -> AccommodationReferralStatus.CANCELLED
    Cas2AssessmentStatus.WITHDRAWN -> AccommodationReferralStatus.WITHDRAWN
    Cas2AssessmentStatus.AWAITING_DECISION -> AccommodationReferralStatus.AWAITING_DECISION
    Cas2AssessmentStatus.ON_WAITING_LIST -> AccommodationReferralStatus.ON_WAITING_LIST
    Cas2AssessmentStatus.OFFER_ACCEPTED -> AccommodationReferralStatus.ACCEPTED
    Cas2AssessmentStatus.OFFER_DECLINED -> AccommodationReferralStatus.OFFER_DECLINED_OR_WITHDRAWN
    else -> AccommodationReferralStatus.PENDING
  }

  fun toStatus(referral: Cas3ReferralHistory): AccommodationReferralStatus = when (referral.bookingStatus) {
    Cas3BookingStatus.DEPARTED -> AccommodationReferralStatus.DEPARTED
    Cas3BookingStatus.CANCELLED -> AccommodationReferralStatus.CANCELLED
    Cas3BookingStatus.NOT_MINUS_ARRIVED,
    Cas3BookingStatus.ARRIVED,
    Cas3BookingStatus.CONFIRMED,
    -> AccommodationReferralStatus.ACCEPTED
    Cas3BookingStatus.PROVISIONAL,
    Cas3BookingStatus.CLOSED,
    null,
    -> when {
      referral.assessmentStatus == AssessmentStatus.CLOSED -> AccommodationReferralStatus.ARCHIVED
      referral.assessmentStatus == AssessmentStatus.REJECTED || referral.applicationStatus == ApplicationStatus.REJECTED -> {
        if (referral.referralRejectionReason != null) {
          AccommodationReferralStatus.REJECTED
        } else {
          AccommodationReferralStatus.ARCHIVED
        }
      }
      else -> AccommodationReferralStatus.PENDING
    }
  }

  fun toStatus(dtr: DutyToReferDto): AccommodationReferralStatus = when (dtr.status) {
    DtrStatus.SUBMITTED -> AccommodationReferralStatus.PENDING
    DtrStatus.ACCEPTED -> AccommodationReferralStatus.ACCEPTED
    DtrStatus.NOT_ACCEPTED -> AccommodationReferralStatus.REJECTED
    DtrStatus.WITHDRAWN -> AccommodationReferralStatus.WITHDRAWN
  }
}
