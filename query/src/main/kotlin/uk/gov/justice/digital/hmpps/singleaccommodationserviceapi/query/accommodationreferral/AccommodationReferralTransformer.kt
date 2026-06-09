package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.StaffDetailsDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory.Cas2Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.TemporaryAccommodationAssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.DeliusUserDto
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

object AccommodationReferralTransformer {
  fun transformReferrals(dto: AccommodationReferralOrchestrationDto, dtrs: List<DutyToReferDto>) = dto.cas1Referrals.map {
    toAccommodationReferralDto(
      id = it.id,
      type = AccommodationService.CAS1,
      status = toCasReferralStatus(it.status),
      date = it.createdAt,
      referralRejectionReason = it.referralRejectionReason,
      referralRejectionReasonDetail = it.referralRejectionReasonDetail,
      localAuthorityArea = it.localAuthorityArea,
      pdu = it.pdu,
      referredBy = it.referredBy,
      placementAddress = it.placementAddress,
      placementStatus = it.placementStatus,
    )
  } +
    dto.cas2Referrals.map {
      toAccommodationReferralDto(
        id = it.id,
        type = AccommodationService.CAS2,
        status = toCasReferralStatus(it.status),
        date = it.createdAt,
        referralRejectionReason = it.referralRejectionReason,
        referralRejectionReasonDetail = it.referralRejectionReasonDetail,
        localAuthorityArea = it.localAuthorityArea,
        pdu = it.pdu,
        referredBy = it.referredBy,
        placementAddress = it.placementAddress,
        placementStatus = it.placementStatus,
      )
    } +
    dto.cas2v2Referrals.map {
      toAccommodationReferralDto(
        id = it.id,
        type = AccommodationService.CAS2v2,
        status = toCasReferralStatus(it.status),
        date = it.createdAt,
        referralRejectionReason = it.referralRejectionReason,
        referralRejectionReasonDetail = it.referralRejectionReasonDetail,
        localAuthorityArea = it.localAuthorityArea,
        pdu = it.pdu,
        referredBy = it.referredBy,
        placementAddress = it.placementAddress,
        placementStatus = it.placementStatus,
      )
    } +
    dto.cas3Referrals.map {
      toAccommodationReferralDto(
        id = it.id,
        type = AccommodationService.CAS3,
        status = toCasReferralStatus(it.status),
        date = it.createdAt,
        referralRejectionReason = it.referralRejectionReason,
        referralRejectionReasonDetail = it.referralRejectionReasonDetail,
        localAuthorityArea = it.localAuthorityArea,
        pdu = it.pdu,
        referredBy = it.referredBy,
        placementAddress = it.placementAddress,
        placementStatus = it.placementStatus,
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
  )

  fun toStaffDetailsDto(referredBy: DeliusUserDto?) = referredBy?.let {
    StaffDetailsDto(
      it.name,
      it.username,
      it.staffCode,
    )
  }

  fun toCasReferralStatus(status: Cas1AssessmentStatus): AccommodationReferralStatus = when (status) {
    Cas1AssessmentStatus.COMPLETED -> AccommodationReferralStatus.ACCEPTED
    Cas1AssessmentStatus.REALLOCATED -> AccommodationReferralStatus.REJECTED
    Cas1AssessmentStatus.AWAITING_RESPONSE,
    Cas1AssessmentStatus.IN_PROGRESS,
    Cas1AssessmentStatus.NOT_STARTED,
    -> AccommodationReferralStatus.PENDING
  }

  fun toCasReferralStatus(status: Cas2Status): AccommodationReferralStatus = when (status) {
    Cas2Status.PLACE_OFFERED,
    Cas2Status.OFFER_ACCEPTED,
    -> AccommodationReferralStatus.ACCEPTED

    Cas2Status.OFFER_DECLINED_OR_WITHDRAWN,
    Cas2Status.REFERRAL_CANCELLED,
    Cas2Status.REFERRAL_WITHDRAWN,
    -> AccommodationReferralStatus.REJECTED

    Cas2Status.MORE_INFORMATION_REQUESTED,
    Cas2Status.AWAITING_ARRIVAL,
    Cas2Status.ON_WAITING_LIST,
    Cas2Status.AWAITING_DECISION,
    -> AccommodationReferralStatus.PENDING
  }

  fun toCasReferralStatus(status: TemporaryAccommodationAssessmentStatus): AccommodationReferralStatus = when (status) {
    TemporaryAccommodationAssessmentStatus.READY_TO_PLACE -> AccommodationReferralStatus.ACCEPTED
    TemporaryAccommodationAssessmentStatus.CLOSED,
    TemporaryAccommodationAssessmentStatus.REJECTED,
    -> AccommodationReferralStatus.REJECTED

    TemporaryAccommodationAssessmentStatus.UNALLOCATED,
    TemporaryAccommodationAssessmentStatus.IN_REVIEW,
    -> AccommodationReferralStatus.PENDING
  }

  fun toCasReferralStatus(status: DtrStatus): AccommodationReferralStatus = when (status) {
    DtrStatus.SUBMITTED -> AccommodationReferralStatus.PENDING
    DtrStatus.ACCEPTED -> AccommodationReferralStatus.ACCEPTED
    DtrStatus.NOT_ACCEPTED -> AccommodationReferralStatus.REJECTED
    DtrStatus.WITHDRAWN -> AccommodationReferralStatus.WITHDRAWN
  }
}
