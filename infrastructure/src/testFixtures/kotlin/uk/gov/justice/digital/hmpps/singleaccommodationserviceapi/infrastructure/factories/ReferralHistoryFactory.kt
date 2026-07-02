package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.DeliusUserDto
import java.time.Instant
import java.util.UUID

fun buildReferralHistory(
  applicationStatus: Cas1ReferralHistory.ApprovedPremisesApplicationStatus,
  requestForPlacementStatus: Cas1ReferralHistory.RequestForPlacementStatus? = null,
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
  referralRejectionReason: String? = null,
  referralRejectionReasonDetail: String? = null,
  localAuthorityArea: String? = null,
  pdu: String? = null,
  referredBy: DeliusUserDto,
  placementAddress: String? = null,
  placementStatus: Cas1ReferralHistory.Cas1SpaceBookingStatus? = null,
  referralUrl: String = "https://example.com/referral",
) = Cas1ReferralHistory(
  id = id,
  applicationId = applicationId,
  applicationStatus = applicationStatus,
  requestForPlacementStatus = requestForPlacementStatus,
  createdAt = createdAt,
  referralRejectionReason = referralRejectionReason,
  referralRejectionReasonDetail = referralRejectionReasonDetail,
  localAuthorityArea = localAuthorityArea,
  pdu = pdu,
  referredBy = referredBy,
  placementAddress = placementAddress,
  placementStatus = placementStatus,
  referralUrl = referralUrl,
)

fun buildReferralHistory(
  applicationStatus: Cas3ReferralHistory.ApplicationStatus,
  id: UUID = UUID.randomUUID(),
  applicationId: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
  referralRejectionReason: String? = null,
  referralRejectionReasonDetail: String? = null,
  localAuthorityArea: String? = null,
  pdu: String? = null,
  referredBy: DeliusUserDto = buildDeliusUserDto(),
  placementAddress: String? = null,
  bookingStatus: Cas3ReferralHistory.Cas3BookingStatus? = null,
  referralUrl: String = "https://example.com/referral",
) = Cas3ReferralHistory(
  id = id,
  applicationId = applicationId,
  applicationStatus = applicationStatus,
  createdAt = createdAt,
  referralRejectionReason = referralRejectionReason,
  referralRejectionReasonDetail = referralRejectionReasonDetail,
  localAuthorityArea = localAuthorityArea,
  pdu = pdu,
  referredBy = referredBy,
  placementAddress = placementAddress,
  bookingStatus = bookingStatus,
  referralUrl = referralUrl,
)

fun buildDeliusUserDto(name: String = "Joe Bloggs", username: String = "user1", staffCode: String = "ABCD1234") = DeliusUserDto(
  name = name,
  username = username,
  staffCode = staffCode,
)
