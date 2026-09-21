package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.ApprovedPremisesApplicationStatus.WITHDRAWN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.DeliusUserDto
import java.time.LocalDate
import java.util.UUID

fun buildReferralHistory(
  applicationStatus: Cas1ReferralHistory.ApprovedPremisesApplicationStatus,
  requestForPlacementStatus: Cas1ReferralHistory.RequestForPlacementStatus? = null,
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  date: LocalDate = LocalDate.now(),
  referralRejectionReason: String? = null,
  referralRejectionReasonDetail: String? = null,
  localAuthorityArea: String? = null,
  pdu: String? = null,
  referredBy: DeliusUserDto,
  placementAddress: String? = null,
  placementStatus: Cas1ReferralHistory.Cas1SpaceBookingStatus? = null,
  uiUrl: String = "https://example.com/referral",
  withdrawalReason: String? = if (applicationStatus == WITHDRAWN) "DuplicatePlacementRequest" else null,
) = Cas1ReferralHistory(
  id = id,
  applicationId = applicationId,
  applicationStatus = applicationStatus,
  requestForPlacementStatus = requestForPlacementStatus,
  date = date,
  referralRejectionReason = referralRejectionReason,
  referralRejectionReasonDetail = referralRejectionReasonDetail,
  localAuthorityArea = localAuthorityArea,
  pdu = pdu,
  referredBy = referredBy,
  placementAddress = placementAddress,
  placementStatus = placementStatus,
  uiUrl = uiUrl,
  withdrawalReason = withdrawalReason,
)

fun buildReferralHistory(
  applicationStatus: Cas3ReferralHistory.ApplicationStatus,
  assessmentStatus: Cas3ReferralHistory.AssessmentStatus,
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  date: LocalDate = LocalDate.now(),
  referralRejectionReason: String? = null,
  referralRejectionReasonDetail: String? = null,
  localAuthorityArea: String? = null,
  pdu: String? = null,
  referredBy: DeliusUserDto = buildDeliusUserDto(),
  placementAddress: String? = null,
  bookingStatus: Cas3ReferralHistory.Cas3BookingStatus? = null,
  uiUrl: String = "https://example.com/referral",
) = Cas3ReferralHistory(
  id = id,
  applicationId = applicationId,
  applicationStatus = applicationStatus,
  assessmentStatus = assessmentStatus,
  date = date,
  referralRejectionReason = referralRejectionReason,
  referralRejectionReasonDetail = referralRejectionReasonDetail,
  localAuthorityArea = localAuthorityArea,
  pdu = pdu,
  referredBy = referredBy,
  placementAddress = placementAddress,
  bookingStatus = bookingStatus,
  uiUrl = uiUrl,
)

fun buildReferralHistory(
  applicationStatus: String = "submitted",
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  applicationSubmittedDate: LocalDate = LocalDate.now(),
  applicationLastUpdatedDate: LocalDate = LocalDate.now(),
  referralRejectionReason: String? = null,
  localAuthorityArea: String? = null,
  pdu: String? = null,
  referredBy: DeliusUserDto = buildDeliusUserDto(),
  placementAddress: String? = null,
  uiUrl: String = "https://example.com/referral",
) = Cas2ReferralHistory(
  id = id,
  applicationId = applicationId,
  applicationStatus = applicationStatus,
  applicationSubmittedDate = applicationSubmittedDate,
  applicationLastUpdatedDate = applicationLastUpdatedDate,
  referralRejectionReason = referralRejectionReason,
  localAuthorityArea = localAuthorityArea,
  pdu = pdu,
  referredBy = referredBy.name,
  placementAddress = placementAddress,
  uiUrl = uiUrl,
)

fun buildDeliusUserDto(name: String = "Joe Bloggs", username: String = "user1") = DeliusUserDto(
  name = name,
  username = username,
)
