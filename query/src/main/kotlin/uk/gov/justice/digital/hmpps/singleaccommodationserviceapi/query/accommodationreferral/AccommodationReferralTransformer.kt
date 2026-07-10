package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.StaffDetailsDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.ApprovedPremisesApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.Cas1SpaceBookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.DeliusUserDto
import java.time.LocalDate
import java.util.UUID

object AccommodationReferralTransformer {
  fun transformReferrals(dto: AccommodationReferralOrchestrationDto, dtrs: List<DutyToReferDto>) = dto.cas1Referrals.map {
    toAccommodationReferralDto(
      id = it.id,
      type = AccommodationService.CAS1,
      status = toCasReferralStatus(it.placementStatus, it.requestForPlacementStatus, it.applicationStatus),
      requestForPlacementStatus = it.requestForPlacementStatus?.value,
      date = it.date,
      referralRejectionReason = it.referralRejectionReason,
      referralRejectionReasonDetail = it.referralRejectionReasonDetail,
      localAuthorityArea = it.localAuthorityArea,
      pdu = it.pdu,
      referredBy = it.referredBy,
      placementAddress = it.placementAddress,
      placementStatus = it.placementStatus?.value,
      uiUrl = it.uiUrl,
    )
  } +
    dto.cas3Referrals.map {
      toAccommodationReferralDto(
        id = it.id,
        type = AccommodationService.CAS3,
        status = toCasReferralStatus(it.bookingStatus, it.assessmentStatus, it.applicationStatus, it.referralRejectionReason),
        assessmentStatus = it.assessmentStatus?.value,
        requestForPlacementStatus = null,
        date = it.date,
        referralRejectionReason = it.referralRejectionReason,
        referralRejectionReasonDetail = it.referralRejectionReasonDetail,
        localAuthorityArea = it.localAuthorityArea,
        pdu = it.pdu,
        referredBy = it.referredBy,
        placementAddress = it.placementAddress,
        placementStatus = it.bookingStatus?.value,
        uiUrl = it.uiUrl,
      )
    } + dtrs.map {
      toAccommodationReferralDto(
        id = it.submission!!.id,
        type = AccommodationService.DTR,
        status = toCasReferralStatus(it.status),
        requestForPlacementStatus = null,
        date = it.submission!!.submissionDate,
        referralRejectionReason = it.submission!!.withdrawalReason?.name,
        referralRejectionReasonDetail = it.submission!!.withdrawalReasonOther,
        localAuthorityArea = it.submission!!.localAuthority.localAuthorityAreaName,
        pdu = it.submission!!.localAuthority.localAuthorityAreaName,
        referredBy = DeliusUserDto(
          name = it.submission!!.createdBy,
          username = it.submission!!.createdByUsername,
          staffCode = null,
        ),
        placementAddress = null,
        placementStatus = it.submission!!.outcomeReason?.name,
        uiUrl = null,
      )
    }

  fun toAccommodationReferralDto(
    id: UUID,
    type: AccommodationService,
    status: AccommodationReferralStatus,
    assessmentStatus: String? = null,
    requestForPlacementStatus: String?,
    date: LocalDate,
    referralRejectionReason: String?,
    referralRejectionReasonDetail: String?,
    localAuthorityArea: String?,
    pdu: String?,
    referredBy: DeliusUserDto?,
    placementAddress: String?,
    placementStatus: String?,
    uiUrl: String?,
  ) = AccommodationReferralDto(
    id = id,
    type = type,
    status = status,
    assessmentStatus = assessmentStatus,
    requestForPlacementStatus = requestForPlacementStatus,
    date = date,
    referralRejectionReason = referralRejectionReason,
    referralRejectionReasonDetail = referralRejectionReasonDetail,
    localAuthorityArea = localAuthorityArea,
    pdu = pdu,
    referredBy = toStaffDetailsDto(referredBy),
    placementAddress = placementAddress,
    placementStatus = placementStatus,
    uiUrl = uiUrl,
  )

  fun toStaffDetailsDto(referredBy: DeliusUserDto?) = referredBy?.let {
    StaffDetailsDto(
      it.name,
      it.username,
      it.staffCode,
    )
  }

  fun toCasReferralStatus(placementStatus: Cas1SpaceBookingStatus?, requestForPlacementStatus: RequestForPlacementStatus?, applicationStatus: ApprovedPremisesApplicationStatus): AccommodationReferralStatus {
    placementStatus?.let {
      return when (it) {
        Cas1SpaceBookingStatus.NOT_ARRIVED -> AccommodationReferralStatus.NOT_ARRIVED
        Cas1SpaceBookingStatus.DEPARTED -> AccommodationReferralStatus.DEPARTED
        Cas1SpaceBookingStatus.CANCELLED -> AccommodationReferralStatus.CANCELLED
        Cas1SpaceBookingStatus.ARRIVED,
        Cas1SpaceBookingStatus.UPCOMING,
        -> AccommodationReferralStatus.ACCEPTED
      }
    }

    requestForPlacementStatus?.let {
      when (it) {
        RequestForPlacementStatus.REQUEST_REJECTED -> return AccommodationReferralStatus.REQUEST_REJECTED
        RequestForPlacementStatus.REQUEST_WITHDRAWN -> return AccommodationReferralStatus.REQUEST_WITHDRAWN
        RequestForPlacementStatus.PLACEMENT_BOOKED -> return AccommodationReferralStatus.ACCEPTED
        RequestForPlacementStatus.REQUEST_UNSUBMITTED,
        RequestForPlacementStatus.REQUEST_SUBMITTED,
        RequestForPlacementStatus.AWAITING_MATCH,
        -> Unit
      }
    }

    return when (applicationStatus) {
      ApprovedPremisesApplicationStatus.EXPIRED -> AccommodationReferralStatus.EXPIRED
      ApprovedPremisesApplicationStatus.WITHDRAWN -> AccommodationReferralStatus.WITHDRAWN
      ApprovedPremisesApplicationStatus.PLACEMENT_ALLOCATED -> AccommodationReferralStatus.ACCEPTED
      ApprovedPremisesApplicationStatus.REJECTED,
      ApprovedPremisesApplicationStatus.INAPPLICABLE,
      -> AccommodationReferralStatus.REJECTED
      else -> AccommodationReferralStatus.PENDING
    }
  }

  fun toCasReferralStatus(bookingStatus: Cas3BookingStatus?, assessmentStatus: AssessmentStatus?, applicationStatus: ApplicationStatus, referralRejectionReason: String?): AccommodationReferralStatus {
    bookingStatus?.let {
      when (it) {
        Cas3BookingStatus.DEPARTED -> return AccommodationReferralStatus.DEPARTED
        Cas3BookingStatus.CANCELLED -> return AccommodationReferralStatus.CANCELLED
        Cas3BookingStatus.NOT_MINUS_ARRIVED,
        Cas3BookingStatus.ARRIVED,
        Cas3BookingStatus.CONFIRMED,
        -> return AccommodationReferralStatus.ACCEPTED
        Cas3BookingStatus.PROVISIONAL,
        Cas3BookingStatus.CLOSED,
        -> Unit
      }
    }

    val isRejected = assessmentStatus == AssessmentStatus.REJECTED ||
      applicationStatus == ApplicationStatus.REJECTED
    if (isRejected) {
      return if (referralRejectionReason != null) {
        AccommodationReferralStatus.REJECTED
      } else {
        AccommodationReferralStatus.ARCHIVED
      }
    }

    return AccommodationReferralStatus.PENDING
  }

  fun toCasReferralStatus(status: DtrStatus): AccommodationReferralStatus = when (status) {
    DtrStatus.SUBMITTED -> AccommodationReferralStatus.PENDING
    DtrStatus.ACCEPTED -> AccommodationReferralStatus.ACCEPTED
    DtrStatus.NOT_ACCEPTED -> AccommodationReferralStatus.REJECTED
    DtrStatus.WITHDRAWN -> AccommodationReferralStatus.WITHDRAWN
  }
}
