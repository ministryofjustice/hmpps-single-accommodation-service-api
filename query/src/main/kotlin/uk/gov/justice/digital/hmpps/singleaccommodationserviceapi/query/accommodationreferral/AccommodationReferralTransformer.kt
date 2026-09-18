package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.StaffDetailsDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.toStaffDetailsDto

object AccommodationReferralTransformer {
  fun transformReferrals(
    dto: AccommodationReferralOrchestrationDto,
    dtrs: List<DutyToReferDto>,
  ): List<AccommodationReferralDto> = buildList {
    addAll(dto.cas1Referrals.map(::toCas1Referral))
    addAll(dto.cas2Referrals.map(::toCas2Referral))
    addAll(dto.cas3Referrals.map(::toCas3Referral))
    addAll(dtrs.mapNotNull(::toDtrReferral))
  }

  private fun toCas1Referral(cas1Referral: Cas1ReferralHistory) = AccommodationReferralDto(
    id = cas1Referral.id,
    type = AccommodationService.CAS1,
    status = AccommodationReferralStatusMapper.toStatus(cas1Referral),
    assessmentStatus = null,
    requestForPlacementStatus = cas1Referral.requestForPlacementStatus?.value,
    date = cas1Referral.date,
    applicationLastUpdatedDate = null,
    referralRejectionReason = cas1Referral.referralRejectionReason,
    referralRejectionReasonDetail = cas1Referral.referralRejectionReasonDetail,
    localAuthorityArea = cas1Referral.localAuthorityArea,
    pdu = cas1Referral.pdu,
    referredBy = cas1Referral.referredBy.toStaffDetailsDto(),
    placementAddress = cas1Referral.placementAddress,
    placementStatus = cas1Referral.placementStatus?.value,
    uiUrl = cas1Referral.uiUrl,
    withdrawalReason = cas1Referral.withdrawalReason,
  )

  private fun toCas2Referral(cas2Referral: Cas2ReferralHistory) = AccommodationReferralDto(
    id = cas2Referral.id,
    type = AccommodationService.CAS2,
    status = AccommodationReferralStatusMapper.toStatus(cas2Referral),
    assessmentStatus = null,
    requestForPlacementStatus = null,
    date = cas2Referral.applicationSubmittedDate,
    applicationLastUpdatedDate = cas2Referral.applicationLastUpdatedDate,
    referralRejectionReason = cas2Referral.referralRejectionReason,
    referralRejectionReasonDetail = null,
    localAuthorityArea = cas2Referral.localAuthorityArea,
    pdu = cas2Referral.pdu,
    referredBy = StaffDetailsDto(
      name = cas2Referral.referredBy,
      username = null,
    ),
    placementAddress = cas2Referral.placementAddress,
    placementStatus = null,
    uiUrl = cas2Referral.uiUrl,
    withdrawalReason = null,
  )

  private fun toCas3Referral(cas3Referral: Cas3ReferralHistory) = AccommodationReferralDto(
    id = cas3Referral.id,
    type = AccommodationService.CAS3,
    status = AccommodationReferralStatusMapper.toStatus(cas3Referral),
    assessmentStatus = cas3Referral.assessmentStatus?.value,
    requestForPlacementStatus = null,
    date = cas3Referral.date,
    applicationLastUpdatedDate = null,
    referralRejectionReason = cas3Referral.referralRejectionReason,
    referralRejectionReasonDetail = cas3Referral.referralRejectionReasonDetail,
    localAuthorityArea = cas3Referral.localAuthorityArea,
    pdu = cas3Referral.pdu,
    referredBy = cas3Referral.referredBy.toStaffDetailsDto(),
    placementAddress = cas3Referral.placementAddress,
    placementStatus = cas3Referral.bookingStatus?.value,
    uiUrl = cas3Referral.uiUrl,
    withdrawalReason = null,
  )

  private fun toDtrReferral(dtr: DutyToReferDto): AccommodationReferralDto? = dtr.submission?.let { submission ->
    AccommodationReferralDto(
      id = submission.id,
      type = AccommodationService.DTR,
      status = AccommodationReferralStatusMapper.toStatus(dtr),
      assessmentStatus = null,
      requestForPlacementStatus = null,
      date = submission.submissionDate,
      applicationLastUpdatedDate = null,
      referralRejectionReason = submission.withdrawalReason?.name,
      referralRejectionReasonDetail = submission.withdrawalReasonOther,
      localAuthorityArea = submission.localAuthority.localAuthorityAreaName,
      pdu = submission.localAuthority.localAuthorityAreaName,
      referredBy = StaffDetailsDto(
        name = submission.createdBy,
        username = submission.createdByUsername,
      ),
      placementAddress = null,
      placementStatus = submission.outcomeReason?.name,
      uiUrl = null,
      withdrawalReason = submission.withdrawalReason?.name,
    )
  }
}
