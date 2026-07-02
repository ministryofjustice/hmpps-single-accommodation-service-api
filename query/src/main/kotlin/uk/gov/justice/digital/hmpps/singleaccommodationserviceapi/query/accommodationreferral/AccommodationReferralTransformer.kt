package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.StaffDetailsDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.DeliusUserDto
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

object AccommodationReferralTransformer {
  fun transformReferrals(dto: AccommodationReferralOrchestrationDto, dtrs: List<DutyToReferDto>) = dto.cas1Referrals.map {
    toAccommodationReferralDto(
      id = it.id,
      type = AccommodationService.CAS1,
      status = toCasReferralStatus(it.applicationStatus),
      date = it.createdAt,
      referralRejectionReason = it.referralRejectionReason,
      referralRejectionReasonDetail = it.referralRejectionReasonDetail,
      localAuthorityArea = it.localAuthorityArea,
      pdu = it.pdu,
      referredBy = it.referredBy,
      placementAddress = it.placementAddress,
      placementStatus = it.placementStatus?.value,
      referralUrl = it.referralUrl,
    )
  } +
    dto.cas3Referrals.map {
      toAccommodationReferralDto(
        id = it.id,
        type = AccommodationService.CAS3,
        status = toCasReferralStatus(it.applicationStatus),
        date = it.createdAt,
        referralRejectionReason = it.referralRejectionReason,
        referralRejectionReasonDetail = it.referralRejectionReasonDetail,
        localAuthorityArea = it.localAuthorityArea,
        pdu = it.pdu,
        referredBy = it.referredBy,
        placementAddress = it.placementAddress,
        placementStatus = it.bookingStatus?.value,
        referralUrl = it.referralUrl,
      )
    } + dtrs.map {
      toAccommodationReferralDto(
        id = it.submission!!.id,
        type = AccommodationService.DTR,
        status = toCasReferralStatus(it.status),
        date = it.submission!!.submissionDate.atStartOfDay().toInstant(ZoneOffset.UTC),
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
        referralUrl = null,
      )
    }

  fun toAccommodationReferralDto(
    id: UUID,
    type: AccommodationService,
    status: AccommodationReferralStatus,
    date: Instant,
    referralRejectionReason: String?,
    referralRejectionReasonDetail: String?,
    localAuthorityArea: String?,
    pdu: String?,
    referredBy: DeliusUserDto?,
    placementAddress: String?,
    placementStatus: String?,
    referralUrl: String?,
  ) = AccommodationReferralDto(
    id = id,
    type = type,
    status = status,
    date = date,
    referralRejectionReason = referralRejectionReason,
    referralRejectionReasonDetail = referralRejectionReasonDetail,
    localAuthorityArea = localAuthorityArea,
    pdu = pdu,
    referredBy = toStaffDetailsDto(referredBy),
    placementAddress = placementAddress,
    placementStatus = placementStatus,
    referralUrl = referralUrl,
  )

  fun toStaffDetailsDto(referredBy: DeliusUserDto?) = referredBy?.let {
    StaffDetailsDto(
      it.name,
      it.username,
      it.staffCode,
    )
  }

  fun toCasReferralStatus(status: Cas1ReferralHistory.ApprovedPremisesApplicationStatus): AccommodationReferralStatus = when (status) {
    Cas1ReferralHistory.ApprovedPremisesApplicationStatus.PLACEMENT_ALLOCATED -> AccommodationReferralStatus.ACCEPTED
    Cas1ReferralHistory.ApprovedPremisesApplicationStatus.REJECTED,
    Cas1ReferralHistory.ApprovedPremisesApplicationStatus.INAPPLICABLE,
    Cas1ReferralHistory.ApprovedPremisesApplicationStatus.WITHDRAWN,
    Cas1ReferralHistory.ApprovedPremisesApplicationStatus.EXPIRED,
    -> AccommodationReferralStatus.REJECTED

    else -> AccommodationReferralStatus.PENDING
  }

  fun toCasReferralStatus(status: Cas3ReferralHistory.ApplicationStatus): AccommodationReferralStatus = when (status) {
    Cas3ReferralHistory.ApplicationStatus.REJECTED -> AccommodationReferralStatus.REJECTED
    else -> AccommodationReferralStatus.PENDING
  }

  fun toCasReferralStatus(status: DtrStatus): AccommodationReferralStatus = when (status) {
    DtrStatus.SUBMITTED -> AccommodationReferralStatus.PENDING
    DtrStatus.ACCEPTED -> AccommodationReferralStatus.ACCEPTED
    DtrStatus.NOT_ACCEPTED -> AccommodationReferralStatus.REJECTED
    DtrStatus.WITHDRAWN -> AccommodationReferralStatus.WITHDRAWN
  }
}
